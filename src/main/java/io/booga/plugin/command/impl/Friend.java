package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Friend extends PluginCommand {

    public final Main plugin;

    public Friend() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.friends";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (args.length > 1) {
                try {
                    Player friend = plugin.getServer().getPlayer(args[1]);
                    if(friend == null) {
                        p.sendMessage("Could not find player: " + args[1]);
                        return;
                    }

                    if(!Main.playerExtensions.get(p).friends.contains(friend.getUniqueId().toString())) {
                        Main.playerExtensions.get(p).friends.add(friend.getUniqueId().toString());
                        Main.plugin.getDataConfig().set("players." + p.getUniqueId().toString() + ".friends",
                                Main.playerExtensions.get(p).friends);
                        p.sendMessage("You've added " + friend.getDisplayName() + " to your friends list.");
                    } else {
                        p.sendMessage(friend.getDisplayName() + " is already on your friends list!");
                    }
                } catch (Exception e) {
                    p.sendMessage("Could not execute command: did you enter a player's name?");
                }
            } else {
                p.sendMessage("Wrong usage! /booga friend <friend name>");
            }
        }
    }
}
