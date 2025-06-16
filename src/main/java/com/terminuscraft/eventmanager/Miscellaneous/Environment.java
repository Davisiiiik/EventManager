package com.terminuscraft.eventmanager.miscellaneous;

public enum Environment {
    /* Dimension types */
    NORMAL, NETHER, THE_END;

    public static Environment fromString(String value) {
        return switch (value.toLowerCase()) {
            case "normal" -> NORMAL;
            case "nether" -> NETHER;
            case "the_end", "theend", "end" -> THE_END;
            default -> throw new IllegalArgumentException("Invalid environment: " + value);
        };
    }

    @Override
    public String toString() {
        return switch (this) {
            case NORMAL -> "normal";
            case NETHER -> "nether";
            case THE_END -> "the_end";
        };
    }
}
