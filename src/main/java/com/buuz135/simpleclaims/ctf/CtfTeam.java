package com.buuz135.simpleclaims.ctf;

import java.awt.*;
import java.util.Locale;

public enum CtfTeam {
    RED("Red", new Color(0xD32F2F).getRGB()),
    BLUE("Blue", new Color(0x1976D2).getRGB()),
    YELLOW("Yellow", new Color(0xFBC02D).getRGB()),
    WHITE("White", new Color(0xEEEEEE).getRGB());

    private final String displayName;
    private final int rgb;

    CtfTeam(String displayName, int rgb) {
        this.displayName = displayName;
        this.rgb = rgb;
    }

    public String displayName() {
        return displayName;
    }

    public String partyName() {
        return "CTF " + displayName;
    }

    public int rgb() {
        return rgb;
    }

    public static CtfTeam fromString(String input) {
        if (input == null) return null;
        String s = input.trim().toLowerCase(Locale.ROOT);
        return switch (s) {
            case "red" -> RED;
            case "blue" -> BLUE;
            case "yellow" -> YELLOW;
            case "white" -> WHITE;
            default -> null;
        };
    }
}

