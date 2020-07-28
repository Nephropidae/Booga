package io.booga.plugin;

import io.booga.plugin.player.Extension;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;

import java.util.*;

public class PluginEventListener implements Listener {

    public String[][] newcomerBookPagesTemplate = new String[][]{
            new String[]{
                    "Hello, <player_name>!",
                    "Welcome to BoogaMC!",
                    "",
                    "This guide will teach",
                    "you about Booga, what",
                    "to do in Booga, and",
                    "how you can survive.",
                    "",
                    "Booga is a dangerous",
                    "game, where the Gods",
                    "must be satisfied if",
                    "there is to be peace."
            },
            new String[]{
                    "There are 3 modes:",
                    "peace, grace and war",
                    "",
                    "War is dangerous.",
                    "Griefing and PvP is",
                    "enabled. You'll also",
                    "be cursed by the",
                    "Gods every 5 minutes.",
                    "",
                    "Peace time is self",
                    "explanatory. Griefing",
                    "and PvP are disabled."
            },
            new String[]{
                    "Peace time lasts as",
                    "long as players",
                    "offer diamonds to",
                    "the Gods.",
                    "",
                    "Grace time is pretty",
                    "much the same as",
                    "peace. Difference is",
                    "that grace time is the",
                    "medium between war",
                    "and peace time."
            },
            new String[]{
                    "If no diamonds are",
                    "offered before",
                    "grace time ends,",
                    "war time will begin.",
                    "",
                    "If no diamonds are",
                    "offered before",
                    "peace time ends,",
                    "war time will begin."
            },
            new String[]{
                    "Any protection time",
                    "granted during grace",
                    "goes towards peace",
                    "time. Peace time",
                    "begins if players",
                    "offered diamonds",
                    "during grace time.",
                    "",
                    "Each diamond offered",
                    "grants 1 minute of",
                    "protection time and",
                    "5 seconds of",
                    "3 random blessings."
            },
            new String[]{
                    "In Booga, you can",
                    "claim world-chunks",
                    "from the wilderness",
                    "as personal chunks.",
                    "",
                    "Each chunk costs",
                    "$1,000. You can own",
                    "a maximum of",
                    "9 chunks.",
                    "",
                    "You earn $10 for",
                    "every monster slain."
            }
    };

    public ItemStack getNewcomerBook(Player p) {
        List<String> pages = new ArrayList<String>();
        for (int pageNum = 0; pageNum < newcomerBookPagesTemplate.length; pageNum++) {
            String page = "";
            for (int lineNum = 0; lineNum < newcomerBookPagesTemplate[pageNum].length; lineNum++) {
                page += newcomerBookPagesTemplate[pageNum][lineNum] + "\n";
            }
            page = page.replaceAll("<player_name>", p.getName());
            pages.add(page);
        }

        ItemStack writtenBook = new ItemStack(Material.WRITTEN_BOOK);
        BookMeta bookMeta = (BookMeta) writtenBook.getItemMeta();
        bookMeta.setTitle("Booga Guide");
        bookMeta.setAuthor("The Gods");
        bookMeta.setPages(pages);
        writtenBook.setItemMeta(bookMeta);

        return writtenBook;
    }

    @EventHandler
    public void onEntityDeathEvent(EntityDeathEvent e) {
        LivingEntity ent = e.getEntity();
        Player killer = ent.getKiller();
        boolean vaultEnabled = Main.econ != null && Main.plugin.getConfig().getBoolean("territory.vault.enabled");
        if (killer != null) {
            if (e.getEntity() instanceof Monster && vaultEnabled) {
                Main.econ.depositPlayer(killer, 10);
            }
        }
    }

    @EventHandler
    public void onPlayerQuitEvent(PlayerQuitEvent e) {
        Player p = e.getPlayer();
        Extension es = Main.playerExtensions.get(p);
        Main.plugin.getDataConfig().set("players." + p.getUniqueId() + ".friends", es.friends);
    }

    @EventHandler
    public void onPlayerJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();

        if (!Main.plugin.getDataConfig().contains("players." + p.getUniqueId())) {
            Main.plugin.getDataConfig().set("players." + p.getUniqueId() + ".total-claims", 0);
            Main.plugin.getDataConfig().set("players." + p.getUniqueId() + ".join-time", new Date().getTime());
            Main.plugin.getDataConfig().set("players." + p.getUniqueId() + ".friends", new ArrayList<String>());

            p.getInventory().addItem(getNewcomerBook(p));

            p.sendMessage("Welcome to BoogaMC!");
        } else {
            p.sendMessage("Welcome back to BoogaMC!");
        }

        Main.playerExtensions.put(p, new Extension());
        if (Main.plugin.getDataConfig().contains("players." + p.getUniqueId() + ".friends")) {
            ArrayList<String> friends = (ArrayList<String>) Main.plugin.getDataConfig().getStringList("players." + p.getUniqueId() + ".friends");
            Main.playerExtensions.get(p).friends = friends;
        }
    }

    @EventHandler
    public void onPlayerInteractEvent(PlayerInteractEvent e) {
        if (e.getPlayer() != null) {

            if (!Main.plugin.getConfig().getString("game-settings.current-mode").equalsIgnoreCase("WAR")) {
                Block block = e.getClickedBlock();

                if (block == null)
                    return;

                String local = new StringBuilder().append(block.getChunk().getX())
                        .append("_")
                        .append(block.getChunk().getZ())
                        .toString();

                if (Main.plugin.getDataConfig().contains("chunks." + local)) {
                    String uuid = Main.plugin.getDataConfig().getString("chunks." + local + ".owner");

                    if (!e.getPlayer().isOp() && uuid.equalsIgnoreCase("server")) {
                        e.setCancelled(true);
                        return;
                    } else if (e.getPlayer().isOp()) {
                        return;
                    }

                    Player owner = Main.plugin.getServer().getPlayer(UUID.fromString(uuid));
                    if (!e.getPlayer().isOp() && !e.getPlayer().equals(owner)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerRespawnEvent(PlayerRespawnEvent e) {
        if (e.isBedSpawn())
            return;
        if (Main.plugin.getConfig().contains("world-spawn")) {
            double x = Main.plugin.getConfig().getInt("world-spawn.x"),
                    y = Main.plugin.getConfig().getInt("world-spawn.y"),
                    z = Main.plugin.getConfig().getInt("world-spawn.z");
            //e.getPlayer().teleport();
            e.setRespawnLocation(new Location(Bukkit.getWorld("world"), x, y, z, -90, 0));
        }
    }

    @EventHandler
    public void onBlockPlaceEvent(BlockPlaceEvent e) {
        if (e.getPlayer() != null) {
            if (!Main.plugin.getConfig().getString("game-settings.current-mode").equalsIgnoreCase("WAR")) {
                Block block = e.getBlock();
                String local = new StringBuilder().append(block.getChunk().getX())
                        .append("_")
                        .append(block.getChunk().getZ()).toString();

                if (Main.plugin.getDataConfig().contains("chunks." + local)) {
                    String uuid = Main.plugin.getDataConfig().getString("chunks." + local + ".owner");

                    if (!e.getPlayer().isOp() && uuid.equalsIgnoreCase("server")) {
                        e.setCancelled(true);
                        return;
                    } else if (e.getPlayer().isOp()) {
                        return;
                    }

                    Player owner = Main.plugin.getServer().getPlayer(UUID.fromString(uuid));
                    if (!e.getPlayer().isOp() && !e.getPlayer().equals(owner)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreakEvent(BlockBreakEvent e) {
        if (e.getPlayer() != null) {
            if (!Main.plugin.getConfig().getString("game-settings.current-mode").equalsIgnoreCase("WAR")) {
                Block block = e.getBlock();

                String local = new StringBuilder().append(block.getChunk().getX())
                        .append("_")
                        .append(block.getChunk().getZ()).toString();

                if (Main.plugin.getDataConfig().contains("chunks." + local)) {
                    String uuid = Main.plugin.getDataConfig().getString("chunks." + local + ".owner");

                    if (!e.getPlayer().isOp() && uuid.equalsIgnoreCase("server")) {
                        e.setCancelled(true);
                        return;
                    } else if (e.getPlayer().isOp()) {
                        return;
                    }

                    Player owner = Main.plugin.getServer().getPlayer(UUID.fromString(uuid));
                    if (!e.getPlayer().isOp() && !e.getPlayer().equals(owner)) {
                        e.setCancelled(true);
                    }
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onEntityExplodeEvent(EntityExplodeEvent e) {
        String global = new StringBuilder().append(e.getLocation().getChunk().getX())
                .append("_")
                .append(e.getLocation().getChunk().getZ()).toString();

        boolean explosivesDisabled = Main.plugin.getDataConfig().getBoolean("chunks." + global + ".tnt-disabled");
        if (explosivesDisabled) {
            e.blockList().clear();
            e.setCancelled(true);
            e.setYield(0.0f);
            return;
        }

        for (Iterator<Block> iterator = e.blockList().iterator(); iterator.hasNext(); ) {
            Block block = iterator.next();

            String local = new StringBuilder().append(block.getChunk().getX())
                    .append("_")
                    .append(block.getChunk().getZ()).toString();

            if (Main.plugin.getDataConfig().contains("chunks." + local)) {
                if (Main.plugin.getDataConfig().getBoolean("chunks." + local + ".tnt-disabled")) {
                    iterator.remove();
                }
            }
        }
    }

    @EventHandler
    public void onEntityDamageEvent(EntityDamageEvent e) {
        DamageCause cause = e.getCause();

        switch (cause) {
            case ENTITY_EXPLOSION:
            case BLOCK_EXPLOSION:
                String local = new StringBuilder().append(e.getEntity().getLocation().getChunk().getX())
                        .append("_")
                        .append(e.getEntity().getLocation().getChunk().getZ()).toString();
                if (Main.plugin.getDataConfig().contains("chunks." + local)) {
                    if (Main.plugin.getDataConfig().getBoolean("chunks." + local + ".tnt-disabled")) {
                        e.setDamage(0.0);
                        e.setCancelled(true);
                    }
                }
                break;
        }
    }

    @EventHandler
    public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e) {
        DamageCause cause = e.getCause();
        if (e.getDamager() instanceof Player) {
            if (!e.getDamager().isOp()) {
                if (!Main.plugin.getConfig()
                        .getString("game-settings.current-mode")
                        .equalsIgnoreCase("WAR")) {
                    e.setDamage(0.0);
                    e.setCancelled(true);
                }
            }

            Player damager = (Player) e.getDamager();
            if (e.getEntity() instanceof Player) {
                Player en = (Player) e.getEntity();
                if (Main.playerExtensions.get(damager).friends.contains(en.getUniqueId().toString())) {
                    e.setDamage(0.0);
                    e.setCancelled(true);
                    damager.sendMessage("You cannot attack your friends!");
                }
            }

        }
    }
}
