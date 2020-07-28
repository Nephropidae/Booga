package io.booga.plugin.command.impl;

import io.booga.plugin.Main;
import io.booga.plugin.command.PluginCommand;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

public class Offer extends PluginCommand {

    public final Main plugin;

    public Offer() {
        plugin = Main.plugin;
    }

    @Override
    public String getPermission() {
        return "booga.offer";
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
                int offeredAmount = 0;

                try {
                    offeredAmount = Integer.parseInt(args[1]);
                } catch (Exception e) {
                    p.sendMessage("Could not offer a malformed number of items.");
                    return;
                }
                boolean donated = false;

                switch (plugin.getConfig().getString("game-settings.current-mode")) {
                    case "WAR":
                        p.sendMessage("§eThe Gods are §cangry§e, and only time will calm them!");
                        break;
                    case "PEACE":
                        long peaceTime = plugin.getConfig().getLong("game-settings.peace-time");
                        ItemStack offeredStack = new ItemStack(Material.DIAMOND, offeredAmount);
                        if (inventoryContains(p.getInventory(), offeredStack)) {
                            removeFromInventory(p.getInventory(), offeredStack);

                            long offeredMilliseconds = offeredAmount * 60000;

                            long offeredMinutes = (offeredMilliseconds / 1000) / 60;
                            long offeredSeconds = (offeredMilliseconds / 1000) % 60;

                            peaceTime += offeredAmount * 60000;

                            donated = true;

                            plugin.getConfig().set("game-settings.peace-time", peaceTime);

                            p.sendMessage("§eThe Gods are greatful. You've offered: " + offeredMinutes + " minutes and " + offeredSeconds + "seconds worth of protection.");

                            // TO-DO: Add random positive effect.
                        } else {
                            p.sendMessage("§eYou do not have §b" + offeredAmount + " diamonds§e.");
                        }
                        break;
                    case "GRACE":
                        long offeredTime = plugin.getConfig().getLong("game-settings.offered-time");
                        offeredStack = new ItemStack(Material.DIAMOND, offeredAmount);
                        if (inventoryContains(p.getInventory(), offeredStack)) {
                            removeFromInventory(p.getInventory(), offeredStack);

                            long offeredMilliseconds = offeredAmount * 30000;

                            long offeredMinutes = (offeredMilliseconds / 1000) / 60;
                            long offeredSeconds = (offeredMilliseconds / 1000) % 60;

                            offeredTime += offeredMilliseconds;

                            donated = true;

                            plugin.getConfig().set("game-settings.offered-time", offeredTime);

                            p.sendMessage("§eThe Gods are greatful. You've offered: " + offeredMinutes + " minutes and " + offeredSeconds + " seconds worth of protection.");

                            // TO-DO: Add random positive effect.
                        } else {
                            p.sendMessage("§eYou do not have §b" + offeredAmount + " diamonds§e.");
                        }
                        break;
                }

                if (donated) {
                    int duration = offeredAmount * 100;

                    for (int i = 0; i < offeredAmount; i++) {
                        PotionEffectType type = Main.boosts[ThreadLocalRandom.current().nextInt(Main.boosts.length)];
                        p.addPotionEffect(new PotionEffect(type, duration, ThreadLocalRandom.current().nextInt(2)));
                    }

                    p.sendMessage("§eThe Gods appreciate your offer and §abless §eyou.");
                }
            } else {
                p.sendMessage("§eMust specify how many §bdiamonds §eyou wish to offer.");
            }
        }
    }
}
