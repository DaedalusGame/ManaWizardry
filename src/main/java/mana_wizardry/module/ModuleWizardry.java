package mana_wizardry.module;

import com.google.common.collect.Lists;
import electroblob.wizardry.constants.Tier;
import mana_wizardry.ConfigManager;
import mana_wizardry.IngredientOr;
import mana_wizardry.ItemWandBotania;
import mana_wizardry.ManaWizardry;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.Ingredient;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.ModelRegistryEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.oredict.OreIngredient;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.util.List;
import java.util.stream.Collectors;

public class ModuleWizardry extends ModuleBase {
    @GameRegistry.ObjectHolder("mana_wizardry:wand_flowers")
    public static Item WAND_FLOWERS;
    @GameRegistry.ObjectHolder("mana_wizardry:wand_livingwood")
    public static Item WAND_LIVINGWOOD;
    @GameRegistry.ObjectHolder("mana_wizardry:wand_dreamwood")
    public static Item WAND_DREAMWOOD;
    @GameRegistry.ObjectHolder("mana_wizardry:wand_terra")
    public static Item WAND_TERRA;


    @SubscribeEvent
    public void registerItems(RegistryEvent.Register<Item> event) {
        WAND_FLOWERS = new ItemWandBotania(Tier.NOVICE, ConfigManager.wandFlowersManaRate).setRegistryName(getRL("wand_flowers")).setUnlocalizedName("wand_flowers");
        WAND_LIVINGWOOD = new ItemWandBotania(Tier.APPRENTICE, ConfigManager.wandLivingwoodManaRate).setRegistryName(getRL("wand_livingwood")).setUnlocalizedName("wand_livingwood");
        WAND_DREAMWOOD = new ItemWandBotania(Tier.ADVANCED, ConfigManager.wandDreamwoodManaRate).setRegistryName(getRL("wand_dreamwood")).setUnlocalizedName("wand_dreamwood");
        WAND_TERRA = new ItemWandBotania(Tier.MASTER, ConfigManager.wandTerraManaRate).setRegistryName(getRL("wand_terra")).setUnlocalizedName("wand_terra");

        event.getRegistry().register(WAND_FLOWERS);
        event.getRegistry().register(WAND_LIVINGWOOD);
        event.getRegistry().register(WAND_DREAMWOOD);
        event.getRegistry().register(WAND_TERRA);
    }

    @SubscribeEvent
    public void registerRecipes(RegistryEvent.Register<IRecipe> event) {
        List<String> petalOreDicts = Lists.newArrayList("petalWhite", "petalOrange", "petalMagenta", "petalLightBlue", "petalYellow", "petalLime", "petalPink", "petalGray", "petalLightGray", "petalCyan", "petalPurple", "petalBlue", "petalBrown", "petalGreen", "petalRed", "petalBlack");
        Ingredient anyPetal = new IngredientOr(petalOreDicts.stream().map(OreIngredient::new).collect(Collectors.toList()));

        event.getRegistry().register(new ShapedOreRecipe(getRL("wand_flowers"),new ItemStack(WAND_FLOWERS,1),true,new Object[]{
                " FF", " IF", "I  ",
                'I', "livingwoodTwig",
                'F', anyPetal}).setRegistryName(getRL("wand_flowers")));
        event.getRegistry().register(new ShapedOreRecipe(getRL("wand_livingwood"),new ItemStack(WAND_LIVINGWOOD,1),true,new Object[]{
                "  T", " I ", "C  ",
                'I', "livingwoodTwig",
                'T', "manaDiamond",
                'C', "nuggetManasteel"}).setRegistryName(getRL("wand_livingwood")));
        event.getRegistry().register(new ShapedOreRecipe(getRL("wand_dreamwood"),new ItemStack(WAND_DREAMWOOD,1),true,new Object[]{
                "  T", " I ", "C  ",
                'I', "dreamwoodTwig",
                'T', "elvenDragonstone",
                'C', "nuggetElvenElementium"}).setRegistryName(getRL("wand_dreamwood")));
        event.getRegistry().register(new ShapedOreRecipe(getRL("wand_terra"),new ItemStack(WAND_TERRA,1),true,new Object[]{
                "  T", " I ", "C  ",
                'I', "livingwoodTwig",
                'T', "ingotTerrasteel",
                'C', "nuggetTerrasteel"}).setRegistryName(getRL("wand_terra")));
    }

    @SideOnly(Side.CLIENT)
    @SubscribeEvent
    public void registerModels(ModelRegistryEvent event) {
        registerItemModel(WAND_FLOWERS, 0, "inventory");
        registerItemModel(WAND_LIVINGWOOD, 0, "inventory");
        registerItemModel(WAND_DREAMWOOD, 0, "inventory");
        registerItemModel(WAND_TERRA, 0, "inventory");
    }
}
