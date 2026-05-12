package com.anngtv.taixiu.models;

public enum BetType {
    TAI("Tài"),
    XIU("Xỉu");

    private final String name;

    BetType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
