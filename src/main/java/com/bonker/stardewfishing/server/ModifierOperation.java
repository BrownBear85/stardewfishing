package com.bonker.stardewfishing.server;

import com.mojang.serialization.Codec;

import javax.annotation.Nullable;
import java.text.DecimalFormat;

public record ModifierOperation(Type type, double value) {
    public static final ModifierOperation DEFAULT = new ModifierOperation(Type.ADDITION, 0);
    public static final Codec<ModifierOperation> CODEC = Codec.STRING.xmap(ModifierOperation::parse, ModifierOperation::toString);
    private static final String DEFAULT_NAME = "default";
    private static final DecimalFormat FORMAT = new DecimalFormat("#.##");

    public double apply(double num) {
        return type.apply(num, value);
    }

    public float apply(float num) {
        return (float) type.apply(num, value);
    }

    public int apply(int num) {
        return (int) type.apply(num, value);
    }

    public boolean matters() {
        return !equals(DEFAULT);
    }

    public boolean isPositive() {
        return type == Type.ADDITION || (type == Type.MULTIPLICATION && value >= 1);
    }

    public ModifierOperation merge(ModifierOperation other) {
        if (matters() && !other.matters()) {
            return this;
        } else if ((!matters() && other.matters()) || type != other.type) {
            return other;
        } else {
            return switch (type) {
                case ADDITION, SUBTRACTION -> new ModifierOperation(type, value + other.value);
                case MULTIPLICATION -> new ModifierOperation(type, value * other.value);
            };
        }
    }

    public String toString(double scale) {
        return this == DEFAULT ? DEFAULT_NAME : type.identifier + FORMAT.format(value * scale);
    }

    @Override
    public String toString() {
        return toString(1);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof ModifierOperation other) {
            return this.type == other.type && this.value == other.value;
        }
        return false;
    }

    public static ModifierOperation parse(String str) {
        if (str == null || str.length() < 2) {
            throw new ModifierOperationException("Found invalid string: '" + str + "'. String must be at least 2 characters.");
        }

        if (str.equals(DEFAULT_NAME)) {
            return DEFAULT;
        }

        char firstChar = str.charAt(0);

        if (Character.isDigit(firstChar)) {
            throw new ModifierOperationException("Found invalid string: '" + str + "'. Operations must be prefixed by a valid operation ('+', '-', or 'x').");
        }

        Type type = Type.fromChar(firstChar);
        if (type == null) {
            throw new ModifierOperationException("Found invalid string: '" + str + "'. Unknown operation '" + firstChar + "', valid operations are '+', '-', and 'x'.");
        }

        double value;
        try {
            value = Double.parseDouble(str.substring(1));
        } catch (NumberFormatException e) {
            throw new ModifierOperationException("Found invalid string: '" + str + "'. ", e);
        }

        if (value < 0) {
            throw new ModifierOperationException("Found invalid string: '" + str + "'. Values must be positive.");
        }

        if ((type == Type.ADDITION || type == Type.SUBTRACTION) && value == 0) {
            return DEFAULT;
        } else if (type == Type.MULTIPLICATION && value == 1) {
            return DEFAULT;
        }

        return new ModifierOperation(type, value);
    }

    public enum Type {
        ADDITION('+'), SUBTRACTION('-'), MULTIPLICATION('x');

        private final char identifier;

        Type(char identifier) {
            this.identifier = identifier;
        }

        public double apply(double base, double value) {
            return switch (this) {
                case ADDITION -> base + value;
                case SUBTRACTION -> base - value;
                case MULTIPLICATION -> base * value;
            };
        }

        @Nullable
        public static Type fromChar(char identifier) {
            return switch (identifier) {
                case '+' -> ADDITION;
                case '-' -> SUBTRACTION;
                case 'x' -> MULTIPLICATION;
                default -> null;
            };
        }
    }

    public static class ModifierOperationException extends RuntimeException {
        public ModifierOperationException(String message) {
            super(message);
        }

        public ModifierOperationException(String message, Throwable throwable) {
            super(message, throwable);
        }
    }
}
