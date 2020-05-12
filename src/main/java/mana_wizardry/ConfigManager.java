package mana_wizardry;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    public static Configuration config;

    public static double wandFlowersManaRate;
    public static double wandLivingwoodManaRate;
    public static double wandDreamwoodManaRate;
    public static double wandTerraManaRate;

    public static Map<String, Double> itemConsumeClickBlock = new HashMap<>();
    public static Map<String, Double> itemConsumeClickEmpty = new HashMap<>();
    public static Map<String, Double> itemConsumeClickEntity = new HashMap<>();
    public static Map<String, Double> itemConsumeWorn = new HashMap<>();
    public static Map<String, Double> itemConsumeArrow = new HashMap<>();
    public static Map<String, Double> itemConsumeStart = new HashMap<>();
    public static Map<String, Double> itemConsumeTick = new HashMap<>();
    public static Map<String, Double> itemConsumeEnd = new HashMap<>();
    public static Map<String, Double> itemConsumeFinish = new HashMap<>();
    public static Map<String, Double> itemConsumeAttack = new HashMap<>();
    public static Map<String, Double> itemConsumeBreak = new HashMap<>();
    public static Map<String, Double> itemConsumeRepair = new HashMap<>();

    public static void init(File configFile)
    {
        MinecraftForge.EVENT_BUS.register(ConfigManager.class);

        if(config == null)
        {
            config = new Configuration(configFile);
            load();
        }
    }

    public static void load() {
        wandFlowersManaRate = config.get("wand.flowers", "manaRate",50, "Multiplier for how much botania mana should be consumed per point of spell cost").getDouble();
        wandLivingwoodManaRate = config.get("wand.livingwood", "manaRate",50, "Multiplier for how much botania mana should be consumed per point of spell cost").getDouble();
        wandDreamwoodManaRate = config.get("wand.dreamwood", "manaRate",50, "Multiplier for how much botania mana should be consumed per point of spell cost").getDouble();
        wandTerraManaRate = config.get("wand.terra", "manaRate",50, "Multiplier for how much botania mana should be consumed per point of spell cost").getDouble();

        config.setCategoryComment("item", "All of the following configs should use the syntax itemid=manacost (ex: minecraft:iron_hoe=10000 will cause iron hoes to consume 10000 mana)");
        itemConsumeClickBlock = getStringIntMap("item", "consumeUseBlock", new String[]{ }, "How much mana should be consumed by items when clicking on blocks. (ex: flint and steel)");
        itemConsumeClickEmpty = getStringIntMap("item", "consumeUseEmpty", new String[]{ }, "How much mana should be consumed by items when clicking in empty space. (ex: ender pearls)");
        itemConsumeClickEntity = getStringIntMap("item", "consumeUseEntity", new String[]{ }, "How much mana should be consumed by items when clicking on entities. (ex: shears)");
        itemConsumeStart = getStringIntMap("item", "consumeStart", new String[]{ }, "How much mana should be consumed by items when starting to use them.");
        itemConsumeTick = getStringIntMap("item", "consumeTick", new String[]{ }, "How much mana should be consumed by items while they are used.");
        itemConsumeFinish = getStringIntMap("item", "consumeFinish", new String[]{ }, "How much mana should be consumed by items when finishing to use them. (ex: food)");
        itemConsumeEnd = getStringIntMap("item", "consumeEnd", new String[]{ }, "How much mana should be consumed by items when stopping to use them. (ex: bow, but it's not recommended, use consumeArrow instead)");
        itemConsumeRepair = getStringIntMap("item", "consumeRepair", new String[]{ }, "Items in this list will be repaired every tick at the cost of mana.");
        itemConsumeBreak = getStringIntMap("item", "consumeBreak", new String[]{ }, "How much mana should be consumed by items when breaking blocks.");
        itemConsumeAttack = getStringIntMap("item", "consumeAttack", new String[]{ }, "How much mana should be consumed by items when attacking.");
        itemConsumeArrow = getStringIntMap("item", "consumeArrow", new String[]{ }, "How much mana should be consumed by bows when firing an arrow.");
        itemConsumeWorn = getStringIntMap("item", "consumeWorn", new String[]{ }, "How much mana should be consumed by items when wearing them. Running out of mana will unequip the armor.");

        if (config.hasChanged())
        {
            config.save();
        }
    }

    private static Map<String, Double> getStringIntMap(String category, String key, String[] _default, String desc) {
        String[] strings = config.get(category, key, _default, desc).getStringList();
        HashMap<String, Double> map = new HashMap<>();
        for (String s : strings) {
            String[] a = s.split("=");
            if (a.length == 2) {
                map.put(a[0], Double.parseDouble(a[1]));
            }
        }
        return map;
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event)
    {
        if(event.getModID().equalsIgnoreCase(ManaWizardry.MODID))
        {
            load();
        }
    }
}
