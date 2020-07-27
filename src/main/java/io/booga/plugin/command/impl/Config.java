package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;

import java.io.IOException;

public class Config extends PluginCommand {

    public final Main plugin;

    public Config() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.config";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (args.length > 1) {
                switch (args[1]) {
                    case "save":
                        try {
                            plugin.getConfig().save(plugin.configFile);
                            plugin.getDataConfig().save(plugin.dataConfigFile);
                            p.sendMessage("Config saved!");
                        } catch (IOException e) {
                            p.sendMessage("Error occurred when saving config.");
                            e.printStackTrace();
                        }
                        break;
                    case "reload":
                        try {
                            plugin.getConfig().load(plugin.configFile);
                            plugin.getDataConfig().load(plugin.dataConfigFile);
                            p.sendMessage("Config reloaded!");
                        } catch (IOException | InvalidConfigurationException e) {
                            p.sendMessage("Error occurred when reloading config.");
                            e.printStackTrace();
                        }
                        break;
                    default:
                        p.sendMessage("Example: /booga config <save/reload>");
                        break;
                }
            }
        }
    }
}
