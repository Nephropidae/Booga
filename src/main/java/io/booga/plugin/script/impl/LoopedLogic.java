package io.booga.plugin.script.impl;

import io.booga.plugin.Main;
import io.booga.plugin.script.Script;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Date;

public class LoopedLogic extends Script {

    public Date date;

    ///summon lightning_bolt 279.0 71 -232.0
    @Override
    public void run() {
        date = new Date();
        switch (Main.plugin.getConfig().getString("game-settings.current-mode")) {
            case "WAR":

                long warTime = Main.plugin.getConfig().getLong("game-settings.war-time");
                long currentTime = date.getTime();

                if (warTime <= currentTime) {
                    long gracePeriod = Main.plugin.getConfig().getLong("timers.grace-period");
                    long graceTime = currentTime + gracePeriod;

                    Main.plugin.getConfig().set("game-settings.current-mode", "GRACE");
                    Main.plugin.getConfig().set("game-settings.grace-time", graceTime);

                    Main.plugin.getConfig().set("game-settings.war-time", (long) 0);

                    try {
                        World w = Bukkit.getWorld("world");
                        Location locale = new Location(w, 279.0, 71, -232.0);
                        w.strikeLightningEffect(locale);
                    } catch (Exception e) {
                    }

                    for (Player p : Main.plugin.getServer().getOnlinePlayers()) {
                        p.sendMessage("[The Gods] We forgive your disobedience.");
                        p.sendMessage("[The Gods] You now have a chance to satisfy us.");
                    }

                }
                break;
            case "PEACE":
                long peaceTime = Main.plugin.getConfig().getLong("game-settings.peace-time");
                currentTime = date.getTime();

                if (peaceTime <= currentTime) {
                    long warPeriod = Main.plugin.getConfig().getLong("timers.war-period");
                    warTime = currentTime + warPeriod;

                    Main.plugin.getConfig().set("game-settings.current-mode", "WAR");
                    Main.plugin.getConfig().set("game-settings.war-time", warTime);

                    Main.plugin.getConfig().set("game-settings.peace-time", (long) 0);

                    try {
                        World w = Bukkit.getWorld("world");
                        Location locale = new Location(w, 279.0, 71, -232.0);
                        w.strikeLightningEffect(locale);
                    } catch (Exception e) {
                    }

                    for (Player p : Main.plugin.getServer().getOnlinePlayers()) {
                        p.sendMessage("[The Gods] You have forgotten us, so we curse you!");
                        p.sendMessage("[The Gods] We no longer offer you protection from others.");
                    }
                }
                break;
            case "GRACE":
                long graceTime = Main.plugin.getConfig().getLong("game-settings.grace-time");
                currentTime = date.getTime();

                if (graceTime <= currentTime) {
                    long peacePeriod = Main.plugin.getConfig().getLong("game-settings.offered-time");
                    peaceTime = currentTime + peacePeriod;

                    if (peacePeriod <= 0) {
                        long warPeriod = Main.plugin.getConfig().getLong("timers.war-period");

                        Main.plugin.getConfig().set("game-settings.current-mode", "WAR");
                        Main.plugin.getConfig().set("game-settings.war-time", currentTime + warPeriod);

                        Main.plugin.getConfig().set("game-settings.grace-time", (long) 0);

                        try {
                            World w = Bukkit.getWorld("world");
                            Location locale = new Location(w, 279.0, 71, -232.0);
                            w.strikeLightningEffect(locale);
                        } catch (Exception e) {
                        }

                        for (Player p : Main.plugin.getServer().getOnlinePlayers()) {
                            p.sendMessage("[The Gods] You have forgotten us, so we curse you!");
                            p.sendMessage("[The Gods] We no longer offer you protection from others.");
                        }
                    } else {
                        Main.plugin.getConfig().set("game-settings.current-mode", "PEACE");
                        Main.plugin.getConfig().set("game-settings.peace-time", currentTime + peacePeriod);

                        Main.plugin.getConfig().set("game-settings.offered-time", (long) 0);

                        Main.plugin.getConfig().set("game-settings.grace-time", (long) 0);

                        try {
                            World w = Bukkit.getWorld("world");
                            Location locale = new Location(w, 279.0, 71, -232.0);
                            w.strikeLightningEffect(locale);
                        } catch (Exception e) {
                        }

                        for (Player p : Main.plugin.getServer().getOnlinePlayers()) {
                            p.sendMessage("[The Gods] You have satisfied us, so we will now protect you.");
                            p.sendMessage("[The Gods] Offer to us and we'll protect you even longer!");
                        }
                    }
                }
        }
    }

    @Override
    public void stop() {
    }

    @Override
    public void process() {
        run();
    }
}
