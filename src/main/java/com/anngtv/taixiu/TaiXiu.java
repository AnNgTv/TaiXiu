package com.anngtv.taixiu;

import com.anngtv.taixiu.commands.TaiXiuCommand;
import com.anngtv.taixiu.manager.GameManager;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class TaiXiu extends JavaPlugin {

    private static TaiXiu instance;
    private static Economy econ = null;
    private GameManager gameManager;
    private com.anngtv.taixiu.database.DatabaseManager databaseManager;

    @Override
    public void onEnable() {
        instance = this;

        if (!setupEconomy()) {
            Logger.getLogger("Minecraft").severe(String.format("[%s] - Disabled due to no Vault dependency found!", getDescription().getName()));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        saveDefaultConfig();

        databaseManager = new com.anngtv.taixiu.database.DatabaseManager(this);
        databaseManager.init();
        
        gameManager = new GameManager(this);
        gameManager.start();

        getCommand("taixiu").setExecutor(new TaiXiuCommand(this));

        getLogger().info("TaiXiu has been enabled!");
    }

    @Override
    public void onDisable() {
        if (gameManager != null) {
            gameManager.stop();
        }
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info("TaiXiu has been disabled!");
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        econ = rsp.getProvider();
        return econ != null;
    }

    public static TaiXiu getInstance() {
        return instance;
    }

    public static Economy getEconomy() {
        return econ;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public com.anngtv.taixiu.database.DatabaseManager getDatabaseManager() {
        return databaseManager;
    }
}
