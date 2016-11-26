package me.pablete1234.arsmea;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ee.ellytr.command.CommandExecutor;
import ee.ellytr.command.CommandRegistry;
import ee.ellytr.command.exception.CommandException;
import me.pablete1234.arsmea.modules.Bank;
import me.pablete1234.arsmea.modules.HeadDrop;
import me.pablete1234.arsmea.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Arsmea extends JavaPlugin {

    private final static String CRAFTBUKKIT_VERSION = "1.11-R0.1-SNAPSHOT";
    private final static String MINECRAFT_VERSION = "1.11";

    private static Arsmea instance;
    private static Gson gson = new GsonBuilder()./*enableComplexMapKeySerialization().*/create();
    private CommandRegistry commandRegistry = new CommandRegistry(this);
    private CommandExecutor commandExecutor;

    private List<Module> loadedModules = Lists.newArrayList();

    public static Arsmea instance() {
        return instance;
    }

    public static Gson getGson() {
        return gson;
    }

    @Override
    public void onEnable() {
        instance = this;
        checkCraftVersion();

        Config.reload(getConfig());
        saveConfig();

        buildModules();
        registerCommands();
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

    private void registerCommands() {
        //ProviderRegistry providerRegistry = commandRegistry.getProviderRegistry();

        commandRegistry.register();
        commandExecutor = new CommandExecutor(commandRegistry.getFactory());
    }


    private void buildModules() {
        buildModules(Arrays.asList(
                HeadDrop.class,
                Bank.class
        ));
    }

    private void buildModules(List<Class<? extends Module>> moduleClasses){
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
        if (module instanceof CommandHandlerModule) {
            Bukkit.broadcastMessage("Registered: " + module.getClass().getSimpleName());
            commandRegistry.addClass(module.getClass());
        }
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

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Config.reload(getConfig());
        saveConfig();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        try {
            commandExecutor.execute(command.getName(), sender, args);
        } catch (CommandException ex) {
            ex.printStackTrace();
        }
        return true;
    }

}
