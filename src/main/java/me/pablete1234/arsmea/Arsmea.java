package me.pablete1234.arsmea;

import com.google.common.collect.Lists;
import me.pablete1234.arsmea.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Arsmea extends JavaPlugin {

    private final static String CRAFTBUKKIT_VERSION = "1.11-R0.1-SNAPSHOT";
    private final static String MINECRAFT_VERSION = "1.11";

    private static Arsmea instance;

    private List<Module> loadedModules = Lists.newArrayList();

    public static Arsmea instance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        checkCraftVersion();

        Config.reload(getConfig());
        saveConfig();

        buildModules();
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Config.reload(getConfig());
        saveConfig();
    }

    private void checkCraftVersion() {
        if (!Bukkit.getServer().getBukkitVersion().equals(CRAFTBUKKIT_VERSION)) {
            getLogger().warning("########################################");
            getLogger().warning("#####  YOUR VERSION OF SPORTBUKKIT #####");
            getLogger().warning("#####  IS NOT SUPPORTED. PLEASE    #####");
            getLogger().warning("#####  USE  SPORTBUKKIT " + MINECRAFT_VERSION + "      #####");
            getLogger().warning("########################################");
        }
    }

    private void buildModules() {
        buildModules(Arrays.asList(
        ));
    }

    private <T extends Module> void buildModules(List<Class<T>> moduleClasses){
        for (Class<? extends Module> cl : moduleClasses) {
            try {
                loadedModules.add(buildModule(cl.getConstructor().newInstance()));
            } catch (Exception e) {
                Bukkit.getLogger().log(Level.SEVERE, cl.getName() + " module could not be built.");
                e.printStackTrace();
            }
        }
    }

    private <T extends Module> T buildModule(T module) {
        if (module instanceof ListenerModule)
            Bukkit.getPluginManager().registerEvents((ListenerModule) module, this);
        module.load();
        return module;
    }

    @Override
    public void onDisable() {
        unloadModules();
        saveConfig();
    }

    private void unloadModules() {
        for (Module module : loadedModules)
            unloadModule(module);
        loadedModules.clear();
    }

    private void unloadModule(Module module) {
        if (module instanceof ListenerModule)
            HandlerList.unregisterAll((ListenerModule) module);
        module.unload();
    }


}
