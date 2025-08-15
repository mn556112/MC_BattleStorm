package com.example.battlestorm;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class StormCommand implements CommandExecutor {
    private final Main plugin;
    private final StormManager stormManager;

    public StormCommand(Main plugin, StormManager stormManager) {
        this.plugin = plugin;
        this.stormManager = stormManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("battlestorm.manage")) {
            sender.sendMessage(ChatColor.RED + "권한이 없습니다.");
            return true;
        }

        if (args.length == 0) {
            sender.sendMessage(ChatColor.YELLOW + "사용법: /storm [start|stop|show|edit|add|remove]");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "start" -> {
                stormManager.startStorm();
                sender.sendMessage(ChatColor.GREEN + "스톰 시작!");
                return true;
            }
            case "stop" -> {
                stormManager.stopStorm();
                sender.sendMessage(ChatColor.RED + "스톰 중지!");
                return true;
            }
            case "show" -> {
                sender.sendMessage(ChatColor.GOLD + "===== 스톰 단계 목록 =====");
                sender.sendMessage(ChatColor.AQUA + "시작 반경: " + plugin.getConfig().getDouble("start-radius"));
                sender.sendMessage(ChatColor.AQUA + "중심 좌표: X=" + plugin.getConfig().getDouble("center.x")
                        + ", Z=" + plugin.getConfig().getDouble("center.z"));
                int index = 1;
                for (StormManager.Phase phase : stormManager.getPhases()) {
                    sender.sendMessage(ChatColor.YELLOW + "" + index + "단계: "
                            + ChatColor.WHITE + "반경 " + phase.getTargetRadius()
                            + ", 대기 " + phase.getDelaySeconds() + "초"
                            + ", 축소 " + phase.getDurationSeconds() + "초"
                            + ", 피해 " + phase.getDamagePerSecond()
                            + ", 경고범위 " + phase.getWarnDistance()
                            + ", 색 " + phase.getColor());
                    index++;
                }
                return true;
            }
            case "edit" -> {
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "사용법: /storm edit <단계번호|'start'|'center'> <속성|here> <값>");
                    return true;
                }

                String target = args[1].toLowerCase();

                // start-radius 수정
                if (target.equals("start")) {
                    if (args.length < 4 || !args[2].equalsIgnoreCase("radius")) {
                        sender.sendMessage(ChatColor.RED + "start의 경우 radius만 수정할 수 있습니다.");
                        return true;
                    }
                    try {
                        double radius = Double.parseDouble(args[3]);
                        plugin.getConfig().set("start-radius", radius);
                        plugin.saveConfig();
                        sender.sendMessage(ChatColor.GREEN + "시작 반경이 " + radius + "로 변경되었습니다.");
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "값 형식이 올바르지 않습니다.");
                    }
                    return true;
                }

                // center 위치 수정
                if (target.equals("center")) {
                    if (!(sender instanceof Player player)) {
                        sender.sendMessage(ChatColor.RED + "콘솔에서는 실행할 수 없습니다.");
                        return true;
                    }
                    if (!args[2].equalsIgnoreCase("here")) {
                        sender.sendMessage(ChatColor.RED + "center 수정은 'here'만 가능합니다. 예: /storm edit center here");
                        return true;
                    }
                    Location loc = player.getLocation();
                    plugin.getConfig().set("center.x", loc.getX());
                    plugin.getConfig().set("center.z", loc.getZ());
                    plugin.saveConfig();
                    sender.sendMessage(ChatColor.GREEN + "중심 좌표가 현재 위치(X=" + loc.getX() + ", Z=" + loc.getZ() + ")로 변경되었습니다.");
                    return true;
                }

                // phase 수정
                int phaseIndex;
                try {
                    phaseIndex = Integer.parseInt(target) - 1;
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "단계 번호는 숫자여야 합니다.");
                    return true;
                }
                if (phaseIndex < 0 || phaseIndex >= stormManager.getPhases().size()) {
                    sender.sendMessage(ChatColor.RED + "존재하지 않는 단계 번호입니다.");
                    return true;
                }

                StormManager.Phase phase = stormManager.getPhases().get(phaseIndex);
                String attr = args[2].toLowerCase();
                String value = args.length >= 4 ? args[3] : "";

                try {
                    switch (attr) {
                        case "radius" -> phase.setTargetRadius(Double.parseDouble(value));
                        case "delay" -> phase.setDelaySeconds(Long.parseLong(value));
                        case "duration" -> phase.setDurationSeconds(Long.parseLong(value));
                        case "damage" -> phase.setDamagePerSecond(Double.parseDouble(value));
                        case "warn" -> phase.setWarnDistance(Double.parseDouble(value));
                        case "color" -> phase.setColor(value.toUpperCase());
                        default -> {
                            sender.sendMessage(ChatColor.RED + "잘못된 속성입니다.");
                            return true;
                        }
                    }
                    sender.sendMessage(ChatColor.GREEN + "단계 " + (phaseIndex + 1) + "의 " + attr + " 값이 변경되었습니다.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "값 형식이 올바르지 않습니다.");
                }
                return true;
            }
            case "add" -> {
                if (args.length < 7) {
                    sender.sendMessage(ChatColor.RED + "사용법: /storm add <반경> <대기> <축소시간> <피해> <경고범위> <색>");
                    return true;
                }
                try {
                    double radius = Double.parseDouble(args[1]);
                    long delay = Long.parseLong(args[2]);
                    long duration = Long.parseLong(args[3]);
                    double damage = Double.parseDouble(args[4]);
                    double warn = Double.parseDouble(args[5]);
                    String color = args[6].toUpperCase();
                    stormManager.getPhases().add(new StormManager.Phase(radius, duration, delay, damage, warn, color));
                    sender.sendMessage(ChatColor.GREEN + "새 단계가 추가되었습니다.");
                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "값 형식이 올바르지 않습니다.");
                }
                return true;
            }
            case "remove" -> {
                if (args.length < 2) {
                    sender.sendMessage(ChatColor.RED + "사용법: /storm remove <단계번호>");
                    return true;
                }
                try {
                    int index = Integer.parseInt(args[1]) - 1;
                    if (index < 0 || index >= stormManager.getPhases().size()) {
                        sender.sendMessage(ChatColor.RED + "존재하지 않는 단계 번호입니다.");
                        return true;
                    }
                    stormManager.getPhases().remove(index);
                    sender.sendMessage(ChatColor.GREEN + "" + (index + 1) + "단계가 제거되었습니다.");

                } catch (NumberFormatException e) {
                    sender.sendMessage(ChatColor.RED + "단계 번호는 숫자여야 합니다.");
                }
                return true;
            }
            default -> {
                sender.sendMessage(ChatColor.RED + "알 수 없는 명령어입니다.");
                return true;
            }
        }
    }
}
