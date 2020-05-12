package mana_wizardry;

import electroblob.wizardry.Wizardry;
import electroblob.wizardry.constants.Tier;
import electroblob.wizardry.data.SpellGlyphData;
import electroblob.wizardry.data.WizardData;
import electroblob.wizardry.entity.living.ISummonedCreature;
import electroblob.wizardry.event.SpellCastEvent;
import electroblob.wizardry.item.ISpellCastingItem;
import electroblob.wizardry.item.IWorkbenchItem;
import electroblob.wizardry.packet.PacketCastSpell;
import electroblob.wizardry.packet.WizardryPacketHandler;
import electroblob.wizardry.registry.WizardryTabs;
import electroblob.wizardry.spell.Spell;
import electroblob.wizardry.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.EnumAction;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.List;

public class ItemWandBotania extends Item implements IWorkbenchItem, ISpellCastingItem {
    public static final int BASE_SPELL_SLOTS = 5;
    private static final int CONTINUOUS_TRACKING_INTERVAL = 20;
    private static final float DISCOVERY_PROGRESSION_MODIFIER = 5f;
    private static final float MAX_PROGRESSION_REDUCTION = 0.75f;

    public Tier tier;
    public double manaRate;

    public ItemWandBotania(Tier tier, double manaRate) {
        super();
        setMaxStackSize(1);
        setCreativeTab(WizardryTabs.GEAR);
        this.tier = tier;
        this.manaRate = manaRate;
    }


    @Override
    public Spell getCurrentSpell(ItemStack stack){
        return WandHelper.getCurrentSpell(stack);
    }

    @Override
    public Spell[] getSpells(ItemStack stack){
        return WandHelper.getSpells(stack);
    }

    @Override
    public void selectNextSpell(ItemStack stack){
        WandHelper.selectNextSpell(stack);
    }

    @Override
    public void selectPreviousSpell(ItemStack stack){
        WandHelper.selectPreviousSpell(stack);
    }

    @Override
    public boolean showSpellHUD(EntityPlayer player, ItemStack stack){
        return true;
    }

    @Override
    public boolean canCast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers){

        // Spells can only be cast if the casting events aren't cancelled...
        if(castingTick == 0){
            if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Pre(SpellCastEvent.Source.WAND, spell, caster, modifiers))) return false;
        }else{
            if(MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Tick(SpellCastEvent.Source.WAND, spell, caster, modifiers, castingTick))) return false;
        }

        int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding

        // As of wizardry 4.2 mana cost is only divided over two intervals each second
        if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

        // ...and the wand has enough mana to cast the spell...
        return canConsumeMana(stack,cost,caster) // This comes first because it changes over time
                // ...and the wand is the same tier as the spell or higher...
                && spell.getTier().level <= this.tier.level
                // ...and either the spell is not in cooldown or the player is in creative mode
                && (WandHelper.getCurrentCooldown(stack) == 0 || caster.isCreative());
    }

    @Override
    public boolean cast(ItemStack stack, Spell spell, EntityPlayer caster, EnumHand hand, int castingTick, SpellModifiers modifiers){

        World world = caster.world;

        if(world.isRemote && !spell.isContinuous && spell.requiresPacket()) return false;

        if(spell.cast(world, caster, hand, castingTick, modifiers)){

            if(castingTick == 0) MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Post(SpellCastEvent.Source.WAND, spell, caster, modifiers));

            if(!world.isRemote){

                // Continuous spells never require packets so don't rely on the requiresPacket method to specify it
                if(!spell.isContinuous && spell.requiresPacket()){
                    // Sends a packet to all players in dimension to tell them to spawn particles.
                    IMessage msg = new PacketCastSpell.Message(caster.getEntityId(), hand, spell, modifiers);
                    WizardryPacketHandler.net.sendToDimension(msg, world.provider.getDimension());
                }

                caster.setActiveHand(hand);

                // Mana cost
                int cost = (int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f); // Weird floaty rounding
                // As of wizardry 4.2 mana cost is only divided over two intervals each second
                if(spell.isContinuous) cost = getDistributedCost(cost, castingTick);

                if(cost > 0) this.consumeMana(stack, cost, caster);

            }

            // Cooldown
            if(!spell.isContinuous && !caster.isCreative()){ // Spells only have a cooldown in survival
                WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * 1.0));
            }

            // Progression
            if(this.tier.level < Tier.MASTER.level && castingTick % CONTINUOUS_TRACKING_INTERVAL == 0){
                WizardData.get(caster).trackRecentSpell(spell);
            }

            return true;
        }

        return false;
    }

    private void consumeMana(ItemStack stack, int cost, EntityLivingBase caster) {
        if(caster instanceof EntityPlayer)
            ManaItemHandler.requestManaExactForTool(stack, (EntityPlayer) caster, MathHelper.ceil(cost * manaRate),true);
    }

    private boolean canConsumeMana(ItemStack stack, int cost, EntityLivingBase caster) {
        if(caster instanceof EntityPlayer)
            return ManaItemHandler.requestManaExactForTool(stack, (EntityPlayer) caster, MathHelper.ceil(cost * manaRate),false);
        return false;
    }

    protected static int getDistributedCost(int cost, int castingTick){

        int partialCost;

        if(castingTick % 20 == 0){ // Whole number of seconds has elapsed
            partialCost = cost / 2 + cost % 2; // Make sure cost adds up to the correct value by adding the remainder here
        }else if(castingTick % 10 == 0){ // Something-and-a-half seconds has elapsed
            partialCost = cost/2;
        }else{ // Some other number of ticks has elapsed
            partialCost = 0; // Wands aren't damaged within half-seconds
        }

        return partialCost;
    }

    @Override
    public int getSpellSlotCount(ItemStack itemStack) {
        return BASE_SPELL_SLOTS;
    }

    @Override
    public boolean onApplyButtonPressed(EntityPlayer player, Slot centre, Slot crystals, Slot upgrade, Slot[] spellBooks) {
        boolean changed = false;

        //No upgrades

        // Reads NBT spell metadata array to variable, edits this, then writes it back to NBT.
        // Original spells are preserved; if a slot is left empty the existing spell binding will remain.
        // Accounts for spells which cannot be applied because they are above the wand's tier; these spells
        // will not bind but the existing spell in that slot will remain and other applicable spells will
        // be bound as normal, along with any upgrades and crystals.
        Spell[] spells = WandHelper.getSpells(centre.getStack());

        if(spells.length <= 0){
            // Base value here because if the spell array doesn't exist, the wand can't possibly have attunement upgrades
            spells = new Spell[BASE_SPELL_SLOTS];
        }

        for(int i = 0; i < spells.length; i++){
            if(spellBooks[i].getStack() != ItemStack.EMPTY){

                Spell spell = Spell.byMetadata(spellBooks[i].getStack().getItemDamage());
                // If the wand is powerful enough for the spell, it's not already bound to that slot and it's enabled for wands
                if(!(spell.getTier().level > this.tier.level) && spells[i] != spell && spell.isEnabled(SpellProperties.Context.WANDS)){
                    spells[i] = spell;
                    changed = true;
                }
            }
        }

        WandHelper.setSpells(centre.getStack(), spells);

        //No charge

        return changed;
    }

    @Override
    public boolean showTooltip(ItemStack stack){
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public net.minecraft.client.gui.FontRenderer getFontRenderer(ItemStack stack){
        return Wizardry.proxy.getFontRenderer(stack);
    }

    //Clicking and stuff

    @Override
    public ActionResult<ItemStack> onItemRightClick(World world, EntityPlayer player, EnumHand hand){
        ItemStack stack = player.getHeldItem(hand);

        // Alternate right-click function; overrides spell casting.
        if(this.selectMinionTarget(player, world)) return new ActionResult<>(EnumActionResult.SUCCESS, stack);

        Spell spell = WandHelper.getCurrentSpell(stack);
        SpellModifiers modifiers = this.calculateModifiers(stack, player, spell);

        if(canCast(stack, spell, player, hand, 0, modifiers)){
            // Now we can cast continuous spells with scrolls!
            if(spell.isContinuous){
                if(!player.isHandActive()){
                    player.setActiveHand(hand);
                    // Store the modifiers for use each tick
                    if(WizardData.get(player) != null) WizardData.get(player).itemCastingModifiers = modifiers;
                    // Return the player's held item so spells can change it if they wish (e.g. possession)
                    return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                }
            }else{
                if(cast(stack, spell, player, hand, 0, modifiers)){
                    return new ActionResult<>(EnumActionResult.SUCCESS, player.getHeldItem(hand));
                }
            }
        }

        return new ActionResult<>(EnumActionResult.FAIL, stack);
    }

    @Override
    public void onUsingTick(ItemStack stack, EntityLivingBase user, int count){

        if(user instanceof EntityPlayer){

            EntityPlayer player = (EntityPlayer)user;

            Spell spell = WandHelper.getCurrentSpell(stack);

            SpellModifiers modifiers;

            if(WizardData.get(player) != null){
                modifiers = WizardData.get(player).itemCastingModifiers;
            }else{
                modifiers = this.calculateModifiers(stack, (EntityPlayer)user, spell); // Fallback to the old way, should never be used
            }

            int castingTick = stack.getMaxItemUseDuration() - count;

            // Continuous spells (these must check if they can be cast each tick since the mana changes)
            // Don't call canCast when castingTick == 0 because we already did it in onItemRightClick
            if(spell.isContinuous && (castingTick == 0 || canCast(stack, spell, player, player.getActiveHand(), castingTick, modifiers))){
                cast(stack, spell, player, player.getActiveHand(), castingTick, modifiers);
            }else{
                // Stops the casting if it was interrupted, either by events or because the wand ran out of mana
                player.stopActiveHand();
            }
        }
    }

    @Override
    public void onPlayerStoppedUsing(ItemStack stack, World world, EntityLivingBase user, int timeLeft){

        if(user instanceof EntityPlayer){

            EntityPlayer player = (EntityPlayer)user;

            Spell spell = WandHelper.getCurrentSpell(stack);

            SpellModifiers modifiers;

            if(WizardData.get(player) != null){
                modifiers = WizardData.get(player).itemCastingModifiers;
            }else{
                modifiers = this.calculateModifiers(stack, (EntityPlayer)user, spell); // Fallback to the old way, should never be used
            }

            int castingTick = stack.getMaxItemUseDuration() - timeLeft; // Might as well include this

            int cost = getDistributedCost((int)(spell.getCost() * modifiers.get(SpellModifiers.COST) + 0.1f), castingTick);

            // Still need to check there's enough mana or the spell will finish twice, since running out of mana is
            // handled separately.
            if(spell.isContinuous && spell.getTier().level <= this.tier.level && canConsumeMana(stack,cost,user)){

                MinecraftForge.EVENT_BUS.post(new SpellCastEvent.Finish(SpellCastEvent.Source.WAND, spell, player, modifiers, castingTick));
                spell.finishCasting(world, player, Double.NaN, Double.NaN, Double.NaN, null, castingTick, modifiers);

                if(!player.isCreative()){ // Spells only have a cooldown in survival
                    WandHelper.setCurrentCooldown(stack, (int)(spell.getCooldown() * 1.0));
                }
            }
        }
    }

    @Override
    public boolean itemInteractionForEntity(ItemStack stack, EntityPlayer player, EntityLivingBase entity, EnumHand hand){

        if(player.isSneaking() && entity instanceof EntityPlayer && WizardData.get(player) != null){
            String string = WizardData.get(player).toggleAlly((EntityPlayer)entity) ? "item." + Wizardry.MODID + ":wand.addally"
                    : "item." + Wizardry.MODID + ":wand.removeally";
            if(!player.world.isRemote) player.sendMessage(new TextComponentTranslation(string, entity.getName()));
            return true;
        }

        return false;
    }

    public SpellModifiers calculateModifiers(ItemStack stack, EntityPlayer player, Spell spell){
        SpellModifiers modifiers = new SpellModifiers();

        return modifiers;
    }


    private boolean selectMinionTarget(EntityPlayer player, World world){

        RayTraceResult rayTrace = RayTracer.standardEntityRayTrace(world, player, 16, false);

        if(rayTrace != null && WizardryUtilities.isLiving(rayTrace.entityHit)){

            EntityLivingBase entity = (EntityLivingBase)rayTrace.entityHit;

            // Sets the selected minion's target to the right-clicked entity
            if(player.isSneaking() && WizardData.get(player) != null && WizardData.get(player).selectedMinion != null){

                ISummonedCreature minion = WizardData.get(player).selectedMinion.get();

                if(minion instanceof EntityLiving && minion != entity){
                    // There is now only the new AI! (which greatly improves things)
                    ((EntityLiving)minion).setAttackTarget(entity);
                    // Deselects the selected minion
                    WizardData.get(player).selectedMinion = null;
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int slot, boolean isHeld){
        WandHelper.decrementCooldowns(stack);
    }

    // A proper hook was introduced for this in Forge build 14.23.5.2805 - Hallelujah, finally!
    // The discussion about this was quite interesting, see the following:
    // https://github.com/TeamTwilight/twilightforest/blob/1.12.x/src/main/java/twilightforest/item/ItemTFScepterLifeDrain.java
    // https://github.com/MinecraftForge/MinecraftForge/pull/4834
    // Among the things mentioned were that it can be 'fixed' by doing the exact same hacks that I did, and that
    // returning a result of PASS rather than SUCCESS from onItemRightClick also solves the problem (not sure why
    // though, and again it's not a perfect solution)
    // Edit: It seems that the hacky fix in previous versions actually introduced a wand duplication bug... oops

    @Override
    public boolean canContinueUsing(ItemStack oldStack, ItemStack newStack){
        // Ignore durability changes
        if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return true;
        return super.canContinueUsing(oldStack, newStack);
    }

    @Override
    public boolean shouldCauseBlockBreakReset(ItemStack oldStack, ItemStack newStack){
        // Ignore durability changes
        if(ItemStack.areItemsEqualIgnoreDurability(oldStack, newStack)) return false;
        return super.shouldCauseBlockBreakReset(oldStack, newStack);
    }

    @Override
    // Only called client-side
    // This method is always called on the item in oldStack, meaning that oldStack.getItem() == this
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged){

        // This method does some VERY strange things! Despite its name, it also seems to affect the updating of NBT...

        if(!oldStack.isEmpty() || !newStack.isEmpty()){
            // We only care about the situation where we specifically want the animation NOT to play.
            if(oldStack.getItem() == newStack.getItem() && !slotChanged && oldStack.getItem() instanceof ItemWandBotania
                    && newStack.getItem() instanceof ItemWandBotania
                    && WandHelper.getCurrentSpell(oldStack) == WandHelper.getCurrentSpell(newStack))
                return false;
        }

        return super.shouldCauseReequipAnimation(oldStack, newStack, slotChanged);
    }

    @Override
    public EnumAction getItemUseAction(ItemStack itemstack){
        return WandHelper.getCurrentSpell(itemstack).action;
    }

    @Override
    public int getMaxItemUseDuration(ItemStack stack){
        return 72000;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public void addInformation(ItemStack stack, World world, List<String> text, net.minecraft.client.util.ITooltipFlag advanced){

        EntityPlayer player = net.minecraft.client.Minecraft.getMinecraft().player;
        if (player == null) { return; }

        Spell spell = WandHelper.getCurrentSpell(stack);

        boolean discovered = true;
        if(Wizardry.settings.discoveryMode && !player.isCreative() && WizardData.get(player) != null
                && !WizardData.get(player).hasSpellBeenDiscovered(spell)){
            discovered = false;
        }

        text.add(TextFormatting.GRAY + net.minecraft.client.resources.I18n.format("item." + Wizardry.MODID + ":wand.spell",
                discovered ? TextFormatting.GRAY + spell.getDisplayNameWithFormatting()
                        : TextFormatting.BLUE + SpellGlyphData.getGlyphName(spell, player.world)));
    }
}
