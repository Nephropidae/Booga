package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Help extends PluginCommand {

    public final Main plugin;

    public Help() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.help";
    }

    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            int page = 1;

            if (args.length > 1) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    p.sendMessage("Could not execute command: Malformed page number?");
                }
            }

            p.performCommand("help booga " + page);
        }
    }
}
