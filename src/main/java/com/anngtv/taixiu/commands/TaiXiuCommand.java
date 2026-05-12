package com.anngtv.taixiu.commands;

import com.anngtv.taixiu.TaiXiu;
import com.anngtv.taixiu.models.Bet;
import com.anngtv.taixiu.models.BetType;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class TaiXiuCommand implements CommandExecutor, TabCompleter {

    private final TaiXiu plugin;

    public TaiXiuCommand(TaiXiu plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "bet":
                handleBet(sender, args);
                break;
            case "status":
                handleStatus(sender);
                break;
            case "history":
                handleHistory(sender);
                break;
            case "reload":
                handleReload(sender);
                break;
            default:
                sendHelp(sender);
                break;
        }

        return true;
    }

    private void handleBet(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("Chỉ người chơi mới có thể đặt cược!");
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("taixiu.use")) {
            player.sendMessage(ChatColor.RED + "Bạn không có quyền sử dụng lệnh này!");
            return;
        }

        if (!plugin.getGameManager().isRoundActive()) {
            player.sendMessage(format(plugin.getConfig().getString("messages.no-round")));
            return;
        }

        if (plugin.getGameManager().hasBet(player.getUniqueId())) {
            player.sendMessage(format(plugin.getConfig().getString("messages.already-bet")));
            return;
        }

        if (args.length < 3) {
            player.sendMessage(format(plugin.getConfig().getString("messages.invalid-type")));
            return;
        }

        BetType type;
        try {
            type = BetType.valueOf(args[1].toUpperCase());
        } catch (IllegalArgumentException e) {
            player.sendMessage(format(plugin.getConfig().getString("messages.invalid-type")));
            return;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            player.sendMessage(format(plugin.getConfig().getString("messages.invalid-amount")));
            return;
        }

        double minBet = plugin.getConfig().getDouble("game.min-bet");
        double maxBet = plugin.getConfig().getDouble("game.max-bet");

        if (amount < minBet || amount > maxBet) {
            player.sendMessage(format(plugin.getConfig().getString("messages.invalid-amount")));
            return;
        }

        if (TaiXiu.getEconomy().getBalance(player) < amount) {
            player.sendMessage(format(plugin.getConfig().getString("messages.not-enough-money")));
            return;
        }

        TaiXiu.getEconomy().withdrawPlayer(player, amount);
        plugin.getGameManager().addBet(new Bet(player.getUniqueId(), amount, type));

        player.sendMessage(format(plugin.getConfig().getString("messages.bet-success")
                .replace("{amount}", String.valueOf(amount))
                .replace("{type}", type.getName())));
    }

    private void handleStatus(CommandSender sender) {
        if (plugin.getGameManager().isRoundActive()) {
            sender.sendMessage(ChatColor.GOLD + "=== Trạng thái TaiXiu ===");
            sender.sendMessage(ChatColor.YELLOW + "Thời gian còn lại: " + ChatColor.WHITE + plugin.getGameManager().getTimeLeft() + " giây");
        } else {
            sender.sendMessage(ChatColor.RED + "Hiện không có phiên nào đang diễn ra.");
        }
    }

    private void handleHistory(CommandSender sender) {
        List<String> history = plugin.getDatabaseManager().getHistory(10);
        if (history.isEmpty()) {
            sender.sendMessage(ChatColor.RED + "Chưa có lịch sử phiên nào.");
            return;
        }

        sender.sendMessage(ChatColor.GOLD + "=== Lịch sử 10 phiên gần nhất ===");
        for (String record : history) {
            sender.sendMessage(ChatColor.YELLOW + record);
        }
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("taixiu.admin")) {
            sender.sendMessage(ChatColor.RED + "Bạn không có quyền thực hiện lệnh này!");
            return;
        }

        plugin.reloadConfig();
        sender.sendMessage(ChatColor.GREEN + "Đã tải lại cấu hình TaiXiu!");
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== Lệnh TaiXiu ===");
        sender.sendMessage(ChatColor.YELLOW + "/tx bet <tai|xiu> <tiền> - Đặt cược");
        sender.sendMessage(ChatColor.YELLOW + "/tx status - Xem trạng thái phiên hiện tại");
        sender.sendMessage(ChatColor.YELLOW + "/tx history - Xem lịch sử phiên");
        if (sender.hasPermission("taixiu.admin")) {
            sender.sendMessage(ChatColor.YELLOW + "/tx reload - Tải lại cấu hình");
        }
    }

    private String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            return Arrays.asList("bet", "status", "history", "reload").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("bet")) {
            return Arrays.asList("tai", "xiu").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}
