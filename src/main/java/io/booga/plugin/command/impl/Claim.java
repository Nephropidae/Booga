package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Claim extends PluginCommand {

    public final Main plugin;

    public Claim() {
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
            if (args.length > 1 && args[1].equalsIgnoreCase("server")) {
                if (p.isOp()) {
                    String local = new StringBuilder().append(p.getLocation().getChunk().getX())
                            .append("_")
                            .append(p.getLocation().getChunk().getZ()).toString();

                    if (plugin.getDataConfig().contains("chunks." + local)) {
                        p.sendMessage("This chunk has already been claimed!");
                        return;
                    }

                    plugin.getDataConfig().set("chunks." + local + ".owner", "server");
                    plugin.getDataConfig().set("chunks." + local + ".tnt-disabled", true);

                    p.sendMessage("The server has reserved this chunk.");
                } else {
                    p.sendMessage("You do not have the required permissions to use this command.");
                }
            } else {
                boolean vaultEnabled = Main.plugin.getConfig().getBoolean("territory.vault.enabled");
                int claimCost = Main.plugin.getConfig().getInt("territory.vault.claim-cost");

                String local = new StringBuilder().append(p.getLocation().getChunk().getX())
                        .append("_")
                        .append(p.getLocation().getChunk().getZ()).toString();

                if (plugin.getDataConfig().contains("chunks." + local)) {
                    p.sendMessage("This chunk has already been claimed!");
                    return;
                }

                int maxClaims = Main.plugin.getConfig().getInt("territory.max-claims");
                int totalClaims = Main.plugin.getDataConfig().getInt("players." + p.getUniqueId() + ".total-claims");

                if (totalClaims >= maxClaims) {
                    p.sendMessage("You have too many claims!");
                    return;
                }

                if (Main.econ != null && vaultEnabled) {
                    if (Main.econ.getBalance(p) >= claimCost) {
                        p.sendMessage("$" + claimCost + " has been taken from your account.");
                        Main.econ.depositPlayer(p, -claimCost);
                    } else {
                        p.sendMessage("You need at least $" + claimCost + " to claim a property.");
                        return;
                    }
                }

                plugin.getDataConfig().set("chunks." + local + ".owner", p.getUniqueId().toString());
                plugin.getDataConfig().set("chunks." + local + ".tnt-disabled", false);

                plugin.getDataConfig().set("players." + p.getUniqueId() + ".total-claims", totalClaims + 1);

                p.sendMessage("Successfully claimed this chunk!");
            }
        }
    }
}
