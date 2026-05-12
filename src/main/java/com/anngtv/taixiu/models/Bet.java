package com.anngtv.taixiu.models;

import java.util.UUID;

public class Bet {
    private final UUID playerUUID;
    private final double amount;
    private final BetType type;

    public Bet(UUID playerUUID, double amount, BetType type) {
        this.playerUUID = playerUUID;
        this.amount = amount;
        this.type = type;
    }

    public UUID getPlayerUUID() {
        return playerUUID;
    }

    public double getAmount() {
        return amount;
    }

    public BetType getType() {
        return type;
    }
}
