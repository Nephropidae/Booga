package io.booga.plugin.command;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public abstract class PluginCommand {

    public boolean hasPermission(Player p) {
        return p.isOp() || p.hasPermission(getPermission());
    }

    public abstract String getPermission();

    public abstract void run(CommandSender sender, Command command, String[] args);

}
