package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Iterator;
import java.util.Set;

public class Unclaim extends PluginCommand {

    public final Main plugin;

    public Unclaim() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.claim";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (args.length > 1 && args[1].equalsIgnoreCase("all")) {
                Set<String> ownedChunks = Main.getChunksByOwner(p.getUniqueId().toString());
                Iterator<String> iterator = ownedChunks.iterator();
                if (iterator == null
                        || (iterator != null && !iterator.hasNext())) {
                    p.sendMessage("Something went wrong.");
                    return;
                } else {
                    try {
                        int total0 = ownedChunks.size();
                        int total1 = ownedChunks.size();

                        while (iterator.hasNext()) {
                            plugin.getDataConfig().set("chunks." + iterator.next(), null);
                            total0--;
                        }

                        plugin.getDataConfig().set("players." + p.getUniqueId() + ".total-claims", total0);
                        p.sendMessage("Unclaimed " + (total1 - total0) + " chunks!");
                    } catch (Exception e) {
                        p.sendMessage("Something went wrong.");
                    }
                }
            } else if (args.length == 1) {
                String local = new StringBuilder().append(p.getLocation().getChunk().getX())
                        .append("_")
                        .append(p.getLocation().getChunk().getZ()).toString();

                if (plugin.getDataConfig().contains("chunks." + local)) {
                    String owner = plugin.getDataConfig().getString("chunks." + local + ".owner");
                    if (p.getUniqueId().toString().equals(owner) || p.isOp()) {
                        plugin.getDataConfig().set("chunks." + local, null);
                        int totalClaims = plugin.getDataConfig().getInt("players." + owner + ".total-claims");
                        plugin.getDataConfig().set("players." + owner + ".total-claims", totalClaims - 1);
                        p.sendMessage("Successfully unclaimed this chunk!");
                    } else {
                        p.sendMessage("You do not own this chunk!");
                    }
                } else {
                    p.sendMessage("This chunk is not yet claimed.");
                }
            } else {
                p.sendMessage("Could not unclaim land. Invalid arguments.");
            }
        }
    }
}
