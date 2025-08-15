package com.example.battlestorm;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    private StormManager stormManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.stormManager = new StormManager(this);
        getCommand("storm").setExecutor(new StormCommand(this, stormManager));

        if (getConfig().getBoolean("auto-start", false)) {
            stormManager.startStorm();
            getLogger().info("BattleStorm: 자동 스톰 시작 활성화됨.");
        }
        getLogger().info("BattleStorm enabled.");
    }

    @Override
    public void onDisable() {
        stormManager.stopStorm();
        getLogger().info("BattleStorm disabled.");
    }

    public StormManager getStormManager() {
        return stormManager;
    }
}
