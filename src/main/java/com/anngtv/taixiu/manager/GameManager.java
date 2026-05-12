package com.anngtv.taixiu.manager;

import com.anngtv.taixiu.TaiXiu;
import com.anngtv.taixiu.models.Bet;
import com.anngtv.taixiu.models.BetType;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class GameManager {

    private final TaiXiu plugin;
    private final List<Bet> currentBets = new ArrayList<>();
    private boolean isRoundActive = false;
    private int timeLeft;
    private BukkitTask gameTask;

    public GameManager(TaiXiu plugin) {
        this.plugin = plugin;
    }

    public void start() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!isRoundActive) {
                    startNewRound();
                }
            }
        }.runTaskTimer(plugin, 0, 20 * 5); // Check every 5 seconds if a round needs to start
    }

    public void stop() {
        if (gameTask != null) {
            gameTask.cancel();
        }
    }

    private void startNewRound() {
        isRoundActive = true;
        currentBets.clear();
        timeLeft = plugin.getConfig().getInt("game.duration", 60);

        broadcast(plugin.getConfig().getString("messages.new-round"));

        gameTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (timeLeft <= 0) {
                    endRound();
                    this.cancel();
                    return;
                }

                int broadcastInterval = plugin.getConfig().getInt("game.broadcast-interval", 20);
                if (timeLeft % broadcastInterval == 0 && timeLeft != plugin.getConfig().getInt("game.duration")) {
                    broadcast(plugin.getConfig().getString("messages.time-left").replace("{time}", String.valueOf(timeLeft)));
                }

                timeLeft--;
            }
        }.runTaskTimer(plugin, 0, 20);
    }

    private void endRound() {
        isRoundActive = false;
        
        Random random = new Random();
        int d1 = random.nextInt(6) + 1;
        int d2 = random.nextInt(6) + 1;
        int d3 = random.nextInt(6) + 1;
        int total = d1 + d2 + d3;

        BetType result = (total >= 11) ? BetType.TAI : BetType.XIU;

        String resultMsg = plugin.getConfig().getString("messages.round-ended")
                .replace("{result}", result.getName())
                .replace("{d1}", String.valueOf(d1))
                .replace("{d2}", String.valueOf(d2))
                .replace("{d3}", String.valueOf(d3))
                .replace("{total}", String.valueOf(total));
        
        broadcast(resultMsg);

        // Save to database
        plugin.getDatabaseManager().saveSession(d1, d2, d3, total, result.getName());

        double multiplier = plugin.getConfig().getDouble("game.multiplier", 1.95);

        for (Bet bet : currentBets) {
            Player player = Bukkit.getPlayer(bet.getPlayerUUID());
            if (bet.getType() == result) {
                double winAmount = bet.getAmount() * multiplier;
                TaiXiu.getEconomy().depositPlayer(Bukkit.getOfflinePlayer(bet.getPlayerUUID()), winAmount);
                if (player != null) {
                    player.sendMessage(format(plugin.getConfig().getString("messages.win")
                            .replace("{amount}", String.valueOf(winAmount))
                            .replace("{type}", bet.getType().getName())));
                }
            } else {
                if (player != null) {
                    player.sendMessage(format(plugin.getConfig().getString("messages.lose")
                            .replace("{type}", bet.getType().getName())));
                }
            }
        }
    }

    public void addBet(Bet bet) {
        currentBets.add(bet);
    }

    public boolean hasBet(UUID uuid) {
        return currentBets.stream().anyMatch(bet -> bet.getPlayerUUID().equals(uuid));
    }

    public boolean isRoundActive() {
        return isRoundActive;
    }

    public int getTimeLeft() {
        return timeLeft;
    }

    private void broadcast(String message) {
        if (message == null || message.isEmpty()) return;
        Bukkit.broadcastMessage(format(plugin.getConfig().getString("messages.prefix") + message));
    }

    private String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }
}
