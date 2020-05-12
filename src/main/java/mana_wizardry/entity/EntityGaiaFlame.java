package mana_wizardry.entity;

import electroblob.wizardry.entity.construct.EntityMagicConstruct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import vazkii.botania.common.Botania;
import vazkii.botania.common.core.handler.ModSounds;

import java.util.Iterator;
import java.util.List;

public class EntityGaiaFlame extends EntityMagicConstruct {
    private static final int FADE_TICKS = 30;

    public EntityGaiaFlame(World world) {
        super(world);
    }

    public void onUpdate(){
        this.motionX = 0.0D;
        this.motionY = 0.0D;
        this.motionZ = 0.0D;
        super.onUpdate();
        float range = 2.5F;
        float r = 0.2F;
        float g = 0.0F;
        float b = 0.2F;

        for(int i = 0; i < 6; ++i) {
            Botania.proxy.wispFX(this.posX - (double)range + Math.random() * (double)range * 2.0D, this.posY, this.posZ - (double)range + Math.random() * (double)range * 2.0D, r, g, b, 0.4F, -0.015F, 1.0F);
        }

        if (this.ticksExisted >= 55) {
            this.world.playSound(null, this.posX, this.posY, this.posZ, ModSounds.gaiaTrap, SoundCategory.NEUTRAL, 0.3F, 1.0F);
            float m = 0.35F;
            g = 0.4F;

            for(int i = 0; i < 25; ++i) {
                Botania.proxy.wispFX(this.posX, this.posY + 1.0D, this.posZ, r, g, b, 0.5F, (float)(Math.random() - 0.5D) * m, (float)(Math.random() - 0.5D) * m, (float)(Math.random() - 0.5D) * m);
            }

            if (!this.world.isRemote) {
                List<EntityPlayer> players = this.world.getEntitiesWithinAABB(EntityPlayer.class, new AxisAlignedBB(this.posX - (double)range, this.posY - (double)range, this.posZ - (double)range, this.posX + (double)range, this.posY + (double)range, this.posZ + (double)range));
                Iterator var7 = players.iterator();

                while(var7.hasNext()) {
                    EntityPlayer player = (EntityPlayer)var7.next();
                    player.attackEntityFrom(DamageSource.causeIndirectMagicDamage(this, this.getCaster()), 10.0F);
                    player.addPotionEffect(new PotionEffect(MobEffects.BLINDNESS, 25, 0));
                    PotionEffect wither = new PotionEffect(MobEffects.WITHER, 120, 2);
                    wither.getCurativeItems().clear();
                    player.addPotionEffect(wither);
                }
            }

            this.setDead();
        }
    }
}