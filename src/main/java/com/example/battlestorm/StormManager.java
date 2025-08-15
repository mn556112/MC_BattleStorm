package com.example.battlestorm;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

public class StormManager {
    private final Plugin plugin;
    private final World world;
    private final List<Phase> phases = new ArrayList<>();
    private int currentPhaseIndex = -1;
    private BukkitRunnable task;
    private BukkitRunnable shrinkTask;
    private BossBar bossBar;
    private boolean isRunning = false;

    public StormManager(Plugin plugin) {
        this.plugin = plugin;
        this.world = Bukkit.getWorld(plugin.getConfig().getString("world", "world"));
        loadConfigPhases();
    }

    public void loadConfigPhases() {
        phases.clear();
        var configPhases = plugin.getConfig().getMapList("phases");
        for (var map : configPhases) {
            double radius = ((Number) map.get("radius")).doubleValue();
            long duration = ((Number) map.get("duration")).longValue();
            double damage = ((Number) map.get("damage")).doubleValue();
            double warn = ((Number) map.get("warn")).doubleValue();
            String color = (String) map.get("color");
            long delay = map.containsKey("delay") ? ((Number) map.get("delay")).longValue() : 0;
            phases.add(new Phase(radius, duration, delay, damage, warn, color));
        }
    }

    public void startStorm() {
        if (isRunning) {
            plugin.getLogger().warning("Storm is already running.");
            return;
        }
        isRunning = true;

        // 월드보더 초기화
        WorldBorder border = world.getWorldBorder();
        double startRadius = plugin.getConfig().getDouble("start-radius", 500);
        double centerX = plugin.getConfig().getDouble("center.x", 0.0);
        double centerZ = plugin.getConfig().getDouble("center.z", 0.0);

        border.setCenter(centerX, centerZ);
        border.setSize(startRadius * 2); // 반지름*2=지름

        currentPhaseIndex = 0;
        bossBar = Bukkit.createBossBar("Storm starting...", BarColor.PURPLE, BarStyle.SOLID);
        bossBar.setVisible(true);
        Bukkit.getOnlinePlayers().forEach(bossBar::addPlayer);

        runPhase();
    }

    public void stopStorm() {
        isRunning = false;

        if (task != null) {
            task.cancel();
            task = null;
        }
        if (shrinkTask != null) {
            shrinkTask.cancel();
            shrinkTask = null;
        }

        currentPhaseIndex = -1;

        if (bossBar != null) {
            bossBar.removeAll();
            bossBar = null;
        }

        if (plugin.getConfig().getBoolean("reset-border-on-stop", true)) {
            world.getWorldBorder().reset();
        }
    }

    private void runPhase() {
        if (!isRunning) return;

        if (currentPhaseIndex >= phases.size()) {
            bossBar.setTitle("§c마지막 자기장. 전투 종료까지 유지됩니다.");
            bossBar.setProgress(1.0);
            return;
        }

        Phase phase = phases.get(currentPhaseIndex);
        WorldBorder border = world.getWorldBorder();
        border.setDamageAmount(phase.getDamagePerSecond());
        border.setWarningDistance((int) phase.getWarnDistance());

        // Delay before shrinking
        task = new BukkitRunnable() {
            long countdown = phase.getDelaySeconds();

            @Override
            public void run() {
                if (!isRunning) {
                    cancel();
                    return;
                }

                if (countdown > 0) {
                    bossBar.setTitle("다음 축소까지 " + countdown + "초 (" + (currentPhaseIndex + 1) + "단계)");
                    bossBar.setProgress((double) countdown / phase.getDelaySeconds());
                    countdown--;
                    return;
                }

                // Shrinking phase
                border.setSize(phase.getTargetRadius() * 2, phase.getDurationSeconds());
                shrinkTask = new BukkitRunnable() {
                    long shrinkTime = phase.getDurationSeconds();

                    @Override
                    public void run() {
                        if (!isRunning) {
                            cancel();
                            return;
                        }
                        if (shrinkTime <= 0) {
                            cancel();
                            currentPhaseIndex++;
                            runPhase();
                            return;
                        }
                        bossBar.setTitle("축소 진행 중... 남은 시간 " + shrinkTime + "초 (" + (currentPhaseIndex + 1) + "단계)");
                        bossBar.setProgress((double) shrinkTime / phase.getDurationSeconds());
                        shrinkTime--;
                    }
                };
                shrinkTask.runTaskTimer(plugin, 0L, 20L);

                cancel();
            }
        };
        task.runTaskTimer(plugin, 0L, 20L);
    }

    public List<Phase> getPhases() {
        return phases;
    }

    public static class Phase {
        private double targetRadius;
        private long durationSeconds;
        private long delaySeconds;
        private double damagePerSecond;
        private double warnDistance;
        private String color;

        public Phase(double targetRadius, long durationSeconds, long delaySeconds,
                     double damagePerSecond, double warnDistance, String color) {
            this.targetRadius = targetRadius;
            this.durationSeconds = durationSeconds;
            this.delaySeconds = delaySeconds;
            this.damagePerSecond = damagePerSecond;
            this.warnDistance = warnDistance;
            this.color = color;
        }

        public double getTargetRadius() { return targetRadius; }
        public long getDurationSeconds() { return durationSeconds; }
        public long getDelaySeconds() { return delaySeconds; }
        public double getDamagePerSecond() { return damagePerSecond; }
        public double getWarnDistance() { return warnDistance; }
        public String getColor() { return color; }

        public void setTargetRadius(double targetRadius) { this.targetRadius = targetRadius; }
        public void setDurationSeconds(long durationSeconds) { this.durationSeconds = durationSeconds; }
        public void setDelaySeconds(long delaySeconds) { this.delaySeconds = delaySeconds; }
        public void setDamagePerSecond(double damagePerSecond) { this.damagePerSecond = damagePerSecond; }
        public void setWarnDistance(double warnDistance) { this.warnDistance = warnDistance; }
        public void setColor(String color) { this.color = color; }
    }
}
