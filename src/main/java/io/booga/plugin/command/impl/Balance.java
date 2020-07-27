package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Balance extends PluginCommand {

    public final Main plugin;

    public Balance() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.balance";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (Main.econ != null) {
                p.performCommand("bal");
            } else {
                p.sendMessage("Economy is not enabled at this time.");
            }
        }
    }
}
