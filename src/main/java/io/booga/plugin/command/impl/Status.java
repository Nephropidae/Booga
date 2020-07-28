package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Date;

public class Status extends PluginCommand {

    public final Main plugin;

    public Status() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.status";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            switch (plugin.getConfig().getString("game-settings.current-mode")) {
                case "WAR":
                    long warTime = plugin.getConfig().getLong("game-settings.war-time");
                    long totalMilliseconds = warTime - (new Date().getTime());
                    totalMilliseconds = totalMilliseconds < 0 ? 0 : totalMilliseconds;

                    long minutes = (totalMilliseconds / 1000) / 60;
                    long seconds = (totalMilliseconds / 1000) % 60;

                    p.sendMessage("§eThe Gods are §cangry§e.");
                    p.sendMessage("§eTheir next change will occur in " + minutes + " minutes, and " + seconds + " seconds.");
                    break;
                case "PEACE":
                    long peaceTime = plugin.getConfig().getLong("game-settings.peace-time");
                    totalMilliseconds = peaceTime - (new Date().getTime());
                    totalMilliseconds = totalMilliseconds < 0 ? 0 : totalMilliseconds;

                    minutes = (totalMilliseconds / 1000) / 60;
                    seconds = (totalMilliseconds / 1000) % 60;

                    p.sendMessage("§eThe Gods are §asatisfied§e.");
                    p.sendMessage("§eTheir next change will occur in " + minutes + " minutes, and " + seconds + " seconds.");
                    break;
                case "GRACE":
                    long graceTime = plugin.getConfig().getLong("game-settings.grace-time");
                    totalMilliseconds = graceTime - (new Date().getTime());
                    totalMilliseconds = totalMilliseconds < 0 ? 0 : totalMilliseconds;

                    minutes = (totalMilliseconds / 1000) / 60;
                    seconds = (totalMilliseconds / 1000) % 60;

                    long offeredTime = plugin.getConfig().getLong("game-settings.offered-time");

                    long minutes0 = (offeredTime / 1000) / 60;
                    long seconds1 = (offeredTime / 1000) % 60;

                    p.sendMessage("§eThe Gods are §9calm§e.");
                    p.sendMessage("§eA total of §b" + minutes0 + " minutes, and " + seconds1 + " seconds §ehave been offered for protection.");
                    p.sendMessage("§eTheir next change will occur in " + minutes + " minutes, and " + seconds + " seconds.");
                    break;
            }
        }
    }
}
