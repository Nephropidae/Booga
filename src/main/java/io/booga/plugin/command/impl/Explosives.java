package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Explosives extends PluginCommand {

    public final Main plugin;

    public Explosives() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.toggle-explosives";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (args.length > 1) {
                boolean isDisabled = (args[1].equalsIgnoreCase("off"));
                String local = new StringBuilder().append(p.getLocation().getChunk().getX())
                        .append("_")
                        .append(p.getLocation().getChunk().getZ()).toString();

                if (plugin.getDataConfig().contains("chunks." + local)) {
                    String owner = plugin.getDataConfig().getString("chunks." + local + ".owner");
                    if (p.isOp() || p.getUniqueId().toString().equals(owner)) {
                        if (isDisabled) {
                            plugin.getDataConfig().set("chunks." + local + ".tnt-disabled", true);
                            p.sendMessage("Explosive destruction has been disabled in this chunk.");
                        } else {
                            plugin.getDataConfig().set("chunks." + local + ".tnt-disabled", false);
                            p.sendMessage("Explosive destruction has been enabled in this chunk.");
                        }
                    } else {
                        p.sendMessage("You do not own this chunk!");
                    }
                } else {
                    p.sendMessage("This chunk is not yet claimed.");
                }
            } else {
                p.sendMessage("Wrong number of command arguments. Example:");
                p.sendMessage("/booga explosives <on/off>");
            }
        }
    }
}
