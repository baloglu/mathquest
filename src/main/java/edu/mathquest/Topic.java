package edu.mathquest;

import java.util.List;

enum Topic {
    FRACTIONS("Fractions", "textures/items/cake", List.of(
            "Naming fractions",
            "Equivalent fractions",
            "Comparing fractions",
            "Adding & subtracting",
            "Whole number × fraction")),
    DECIMALS("Tenths & Hundredths", "textures/items/gold_nugget", List.of(
            "Tenths",
            "Hundredths",
            "Tenths = hundredths",
            "Comparing decimals")),
    MULTIPLICATION("Multiplication", "textures/items/diamond", List.of(
            "×1 ×2 ×5 ×10",
            "×3 ×4",
            "×6 ×7 ×8 ×9",
            "Up to 12 × 12",
            "Big numbers & missing factors"));

    final String display;
    final String icon;
    final List<String> levelNames;

    Topic(String display, String icon, List<String> levelNames) {
        this.display = display;
        this.icon = icon;
        this.levelNames = levelNames;
    }

    int maxLevel() {
        return levelNames.size();
    }

    String levelName(int level) {
        return levelNames.get(Math.max(1, Math.min(level, maxLevel())) - 1);
    }

    static Topic parse(String s) {
        return switch (s.toLowerCase()) {
            case "fractions", "fraction", "f" -> FRACTIONS;
            case "decimals", "decimal", "tenths", "hundredths", "d" -> DECIMALS;
            case "multiplication", "times", "multiply", "m" -> MULTIPLICATION;
            default -> null;
        };
    }
}
