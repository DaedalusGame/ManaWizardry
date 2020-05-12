package mana_wizardry;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.ArrowLooseEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import vazkii.botania.api.mana.ManaItemHandler;

import java.util.Map;

public class HandlerBase {
    @SubscribeEvent
    public void onRightClick(PlayerInteractEvent.RightClickItem event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItemStack();

        Map<String, Double> costMap = ConfigManager.itemConsumeClickEmpty;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickEntitySpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItemStack();

        Map<String, Double> costMap = ConfigManager.itemConsumeClickEntity;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickEntity(PlayerInteractEvent.EntityInteract event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItemStack();

        Map<String, Double> costMap = ConfigManager.itemConsumeClickEntity;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItemStack();

        Map<String, Double> costMap = ConfigManager.itemConsumeClickBlock;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onStartUse(LivingEntityUseItemEvent.Start event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItem();

        Map<String, Double> costMap = ConfigManager.itemConsumeStart;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTickUse(LivingEntityUseItemEvent.Tick event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItem();

        Map<String, Double> costMap = ConfigManager.itemConsumeTick;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onStopUse(LivingEntityUseItemEvent.Stop event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItem();

        Map<String, Double> costMap = ConfigManager.itemConsumeEnd;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFinishUse(LivingEntityUseItemEvent.Finish event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getItem();

        Map<String, Double> costMap = ConfigManager.itemConsumeFinish;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onAttack(AttackEntityEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = entity.getHeldItemMainhand();

        Map<String, Double> costMap = ConfigManager.itemConsumeAttack;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onHarvest(PlayerEvent.HarvestCheck event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = entity.getHeldItemMainhand();

        Map<String, Double> costMap = ConfigManager.itemConsumeBreak;
        if (!useMana(entity, stack, costMap, true)) {
            event.setCanHarvest(false);
        }
    }

    @SubscribeEvent
    public void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = entity.getHeldItemMainhand();

        Map<String, Double> costMap = ConfigManager.itemConsumeBreak;
        if (!useMana(entity, stack, costMap, true)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onBreak(BlockEvent.BreakEvent event) {
        EntityLivingBase entity = event.getPlayer();
        ItemStack stack = entity.getHeldItemMainhand();

        Map<String, Double> costMap = ConfigManager.itemConsumeBreak;
        if (useMana(entity, stack, costMap, true)) {
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    private boolean useMana(EntityLivingBase player, ItemStack item, Map<String, Double> costMap, boolean simulate) {
        if (player instanceof EntityPlayer) {
            ResourceLocation registryName = item.getItem().getRegistryName();
            if (item.isEmpty() || registryName == null || !costMap.containsKey(registryName.toString()))
                return true;
            int cost = MathHelper.ceil(costMap.get(registryName.toString()));
            return ManaItemHandler.requestManaExactForTool(item, (EntityPlayer) player, cost, !simulate);
        }
        return true;
    }

    private ItemStack repairWithMana(EntityLivingBase player, ItemStack item, Map<String, Double> costMap) {
        if (item.isItemDamaged() && player instanceof EntityPlayer) {
            ResourceLocation registryName = item.getItem().getRegistryName();
            if (!item.isEmpty() && registryName != null && costMap.containsKey(registryName.toString())) {
                double perDurability = costMap.get(registryName.toString());
                int cost = MathHelper.ceil(item.getItemDamage() * perDurability);
                int mana = ManaItemHandler.requestManaForTool(item, (EntityPlayer) player, MathHelper.floor(cost / perDurability), true);
                item.setItemDamage(Math.max(item.getItemDamage() - mana, 0));
            }
        }
        return item;
    }

    @SubscribeEvent
    public void onArrowLoose(ArrowLooseEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        ItemStack stack = event.getBow();

        Map<String, Double> costMap = ConfigManager.itemConsumeArrow;
        if (useMana(entity, stack, costMap, true)) { //Loosing an arrow can't be detected properly. It's arrow velocity > 0.1 for a vanilla bow, but we can't make any assumptions about other bows.
            useMana(entity, stack, costMap, false);
        } else {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onTick(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if (entity instanceof EntityPlayer)
            for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
                ItemStack item = entity.getItemStackFromSlot(slot);
                //Repair worn stuff and held items
                repairWithMana(entity, item, ConfigManager.itemConsumeRepair);
                //Deal with worn armor
                if (slot.getSlotType() == EntityEquipmentSlot.Type.ARMOR) {
                    if (useMana(entity, item, ConfigManager.itemConsumeWorn, true)) {
                        useMana(entity, item, ConfigManager.itemConsumeWorn, false);
                    } else {
                        entity.setItemStackToSlot(slot, ItemStack.EMPTY);
                        ((EntityPlayer) entity).addItemStackToInventory(item);
                    }
                }
            }
    }
}
