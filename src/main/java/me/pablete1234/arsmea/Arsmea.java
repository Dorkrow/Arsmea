package me.pablete1234.arsmea;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ee.ellytr.command.CommandExecutor;
import ee.ellytr.command.CommandRegistry;
import ee.ellytr.command.exception.CommandConsoleException;
import ee.ellytr.command.exception.CommandException;
import ee.ellytr.command.exception.CommandPermissionException;
import ee.ellytr.command.exception.CommandPlayerException;
import ee.ellytr.command.exception.CommandUsageException;
import me.pablete1234.arsmea.modules.Bank;
import me.pablete1234.arsmea.modules.HeadDrop;
import me.pablete1234.arsmea.util.Config;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;

public class Arsmea extends JavaPlugin {

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

        Config.reload(getConfig());
        saveConfig();

        buildModules();
        registerCommands();
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
        } catch (CommandUsageException e) {
            String error = ChatColor.RED + "";
            switch (e.getError()) {
                case TOO_FEW_ARGUMENTS:
                    error += "Too few arguments.";
                    break;
                case TOO_MANY_ARGUMENTS:
                    error += "Too many arguments.";
                    break;
                case INVALID_USAGE:
                    error += "Invalid usage.";
                    break;
                case INVALID_ARGUMENTS:
                    error += "Invalid argument.";
                    break;
            }
            sender.sendMessage(error);
        } catch (CommandException e) {
            String error = ChatColor.RED + "";
            if (e instanceof CommandConsoleException) {
                error += "Console may not use this command.";
            } else if (e instanceof CommandPlayerException) {
                error += "Player may not use this command.";
            } else if (e instanceof CommandPermissionException) {
                error += "You do not have permissions to run this command.";
            } else {
                error += "Unknown error.";
                e.printStackTrace();
            }
            sender.sendMessage(error);
        }
        return true;
    }

}
