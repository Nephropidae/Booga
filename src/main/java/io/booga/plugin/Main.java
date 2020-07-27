package io.booga.plugin;

import io.booga.plugin.command.PluginCommand;
import io.booga.plugin.command.impl.*;
import io.booga.plugin.player.Extension;
import io.booga.plugin.script.Script;
import io.booga.plugin.script.ScriptHandler;
import io.booga.plugin.script.impl.LoopedLogic;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class Main extends JavaPlugin {

    public static final PotionEffectType[] curses = new PotionEffectType[]{
            PotionEffectType.UNLUCK, PotionEffectType.HUNGER,
            PotionEffectType.CONFUSION, PotionEffectType.SLOW,
            PotionEffectType.WEAKNESS, PotionEffectType.POISON,
            PotionEffectType.GLOWING, PotionEffectType.SLOW_DIGGING
    };
    public static final PotionEffectType[] boosts = new PotionEffectType[]{
            PotionEffectType.FAST_DIGGING, PotionEffectType.NIGHT_VISION,
            PotionEffectType.DAMAGE_RESISTANCE, PotionEffectType.REGENERATION,
            PotionEffectType.WATER_BREATHING, PotionEffectType.SPEED,
            PotionEffectType.LUCK, PotionEffectType.INCREASE_DAMAGE
    };

    public static Main plugin;
    public static Economy econ = null;
    private final Map<String, Class<? extends PluginCommand>> commands = new HashMap();
    public boolean isEnabled = false;
    public File dataConfigFile;
    public File configFile;
    private FileConfiguration dataConfig;

    public static final Map<Player, Extension> playerExtensions = new HashMap();

    public static Set<String> getChunksByOwner(String owner) {
        ConfigurationSection chunks = plugin.getDataConfig().getConfigurationSection("chunks");
        Set<String> setChunks = chunks.getKeys(false);
        Iterator<String> keys = setChunks.iterator();
        Set<String> ownedChunks = new HashSet();

        while (keys.hasNext()) {
            String key = keys.next();
            if (chunks.contains(key)) {
                ConfigurationSection chunk = chunks.getConfigurationSection(key);
                if (chunk != null && chunk.contains("owner")) {
                    String chunkOwner = chunk.getString("owner");
                    if (chunkOwner.equalsIgnoreCase(owner)) {
                        try {
                            ownedChunks.add(chunk.getName());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        return ownedChunks;
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    @Override
    public void onLoad() {
        plugin = this;
        plugin.saveDefaultConfig();
        plugin.configFile = new File(getDataFolder(), "config.yml");
        plugin.createDataConfig();

        commands.put("claim", Claim.class);
        commands.put("unclaim", Unclaim.class);
        commands.put("explosives", Explosives.class);
        commands.put("status", Status.class);
        commands.put("offer", Offer.class);
        commands.put("config", Config.class);
        commands.put("help", Help.class);
        commands.put("bal", Balance.class);
        commands.put("balance", Balance.class);
        commands.put("friend", Friend.class);
        commands.put("unfriend", Unfriend.class);
    }

    @Override
    public void onEnable() {
        plugin.isEnabled = true;
        Script.setPlugin(plugin);

        getServer().getPluginManager().registerEvents(new PluginEventListener(), plugin);

        if (!setupEconomy()) {
            getLogger().severe(String.format("Cannot find an economy plugin! Economic features will not work.", getDescription().getName()));
        }

        activate();

        getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            if (plugin.isEnabled) {
                if (plugin.getConfig().getString("game-settings.current-mode").equalsIgnoreCase("WAR")) {
                    getServer().getOnlinePlayers().forEach(player -> {

                        PotionEffectType[] effects = new PotionEffectType[]{
                                curses[ThreadLocalRandom.current().nextInt(curses.length)],
                                curses[ThreadLocalRandom.current().nextInt(curses.length)],
                                curses[ThreadLocalRandom.current().nextInt(curses.length)]
                        };

                        for (PotionEffectType type : effects) {
                            player.addPotionEffect(new PotionEffect(type, 3000, 0));
                            player.sendMessage("The Gods have cursed you with " + type.getName().toLowerCase() + ".");
                        }

                        effects = null;
                    });
                }
            }
        }, 0l, 6000);
    }

    public final String[] serverRules = new String[] {
            "Respect other players in chat.",
            "No racism / discrimination allowed.",
            "No excessive swearing allowed.",
            "No spam allowed. Keep CAPS to a minimum.",
            "Do not hack / cheat / exploit.",
            "Do not advertise.",
            "Do not abuse any game mechanic that seems unfair.",
            "Operators decide what is fair or inappropriate.",
            "Operators may not enforce rules that do not exist.",
            "If you feel an Operator is abusive, contact Aztekka."
    };

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        super.onCommand(sender, command, label, args);
        Player p = sender.getServer().getPlayer(sender.getName());
        if (plugin.isEnabled) {
            try {
                switch (command.getName()) {
                    case "rules":
                        p.sendMessage("§aBoogaMC Rules:");
                        for(int i = 0; i < serverRules.length; i++)
                            p.sendMessage("§6[" + (i + 1) + "] " + serverRules[i]);
                        return true;
                }
                if (args.length > 0 && commands.get(args[0]) != null)
                    commands.get(args[0]).getConstructor().newInstance().run(sender, command, args);
                else
                    p.performCommand("help booga");
            } catch (InstantiationException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            }
        }
        return true;
    }

    public boolean bordersChunk(int thisX, int thisZ, int otherX, int otherZ) {
        int chX0 = thisX + 1, chX1 = thisX - 1, chZ0 = thisZ + 1, chZ1 = thisZ - 1;
        return (otherX == chX0 || otherX == chX1)
                && (otherZ == chZ0 || otherZ == chZ1);
    }

    @Override
    public void onDisable() {
        clean(false);
    }

    public void activate() {
        if (!plugin.getConfig().contains("world-spawn")) {
            plugin.getConfig().set("world-spawn.x", 0.0);
            plugin.getConfig().set("world-spawn.y", (double) plugin.getServer()
                    .getWorld("world")
                    .getHighestBlockAt(0, 0)
                    .getY());
            plugin.getConfig().set("world-spawn.z", (double) 0);
        }

        ScriptHandler.getScripts().put("LoopedLogic", new LoopedLogic());
        ScriptHandler.processor();
    }

    public void clean(boolean isEnabled) {
        plugin.isEnabled = isEnabled;
        try {
            plugin.getConfig().save(configFile);
            plugin.getDataConfig().save(dataConfigFile);
        } catch (Exception e) {
            e.printStackTrace();
        }
        ScriptHandler.shutdown();
    }

    public FileConfiguration getDataConfig() {
        return this.dataConfig;
    }

    private void createDataConfig() {
        dataConfigFile = new File(getDataFolder(), "data.yml");
        if (!dataConfigFile.exists()) {
            dataConfigFile.getParentFile().mkdirs();
            saveResource("data.yml", false);
        }

        dataConfig = new YamlConfiguration();

        try {
            dataConfig.load(dataConfigFile);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }
}
