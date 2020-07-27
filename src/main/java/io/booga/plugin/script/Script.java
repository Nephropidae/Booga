package io.booga.plugin.script;

import io.booga.plugin.Main;

import java.util.logging.Level;

public abstract class Script {

    protected static Main plugin = null;
    private final boolean running = false;

    public static Main getPlugin() {
        return Script.plugin;
    }

    public static void setPlugin(Main plugin) {
        if (plugin == null)
            Script.plugin = plugin;
    }

    public static void Log(Level type, String message) {
        if (plugin != null) {
            plugin.getLogger().log(type, message);
        } else {
            System.out.println("[The Offering][" + type.getName() + "] " + message);
        }
    }

    public abstract void run();

    public abstract void stop();

    public abstract void process();
}
