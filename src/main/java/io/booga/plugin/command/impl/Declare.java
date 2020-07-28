package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.Date;

public class Declare extends PluginCommand {

    public final Main plugin;

    public Declare() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.declare";
    }

    public void removeFromInventory(Inventory inventory, ItemStack item) {
        int amt = item.getAmount();
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
                if (items[i].getAmount() > amt) {
                    items[i].setAmount(items[i].getAmount() - amt);
                    break;
                } else if (items[i].getAmount() == amt) {
                    items[i] = null;
                    break;
                } else {
                    amt -= items[i].getAmount();
                    items[i] = null;
                }
            }
        }
        inventory.setContents(items);
    }

    private boolean inventoryContains(Inventory inventory, ItemStack item) {
        int count = 0;
        ItemStack[] items = inventory.getContents();
        for (int i = 0; i < items.length; i++) {
            if (items[i] != null && items[i].getType() == item.getType() && items[i].getDurability() == item.getDurability()) {
                count += items[i].getAmount();
            }
            if (count >= item.getAmount()) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void run(CommandSender sender, Command command, String[] args) {
        Player p = sender.getServer().getPlayer(sender.getName());
        if (hasPermission(p)) {
            if (args.length > 1) {
                String mode = plugin.getConfig().getString("game-settings.current-mode");
                long peaceTime = plugin.getConfig().getLong("game-settings.peace-time");
                peaceTime -= new Date().getTime();
                long warTime = plugin.getConfig().getLong("game-settings.war-time");
                warTime -= new Date().getTime();
                switch (args[1]) {
                    case "war":
                        long emeraldsRequired = (long) Math.floor(peaceTime / 40000);
                        if (mode.equalsIgnoreCase("PEACE")) {
                            ItemStack stack = new ItemStack(Material.EMERALD, (int) emeraldsRequired);
                            if (emeraldsRequired > 0 && inventoryContains(p.getInventory(), stack)) {
                                p.sendMessage("§cYour pleas for war have been heard, and has angered the Gods!");
                                plugin.getConfig().set("game-settings.peace-time", (long) 0);
                                removeFromInventory(p.getInventory(), stack);
                            } else {
                                p.sendMessage("§eYou require §a" + emeraldsRequired + " emeralds §eto declare war time!");
                            }
                        } else {
                            p.sendMessage("§eIt must be §apeace time §eto declare §cwar§e!");
                        }
                        break;
                    case "grace":
                        long diamondsRequired = (long) Math.floor(warTime / 30000) * 2;
                        if (mode.equalsIgnoreCase("WAR")) {
                            ItemStack stack = new ItemStack(Material.DIAMOND, (int) diamondsRequired);
                            if (diamondsRequired > 0 && inventoryContains(p.getInventory(), stack)) {
                                p.sendMessage("§eYou have appeased the Gods §canger§e, and they have offered you §9grace§e.");
                                plugin.getConfig().set("game-settings.war-time", (long) 0);
                                removeFromInventory(p.getInventory(), stack);
                            } else {
                                p.sendMessage("§eYou require §b" + diamondsRequired + " diamonds §eto appease the Gods §canger§e!");
                            }
                        } else {
                            p.sendMessage("§eIt must be §cwar time §eto appease the Gods §canger§e!");
                        }
                        break;
                }
            } else {
                p.sendMessage("§eMust specify a mode of either §cwar §eor §9grace §eto proceed.");
            }
        }
    }
}
