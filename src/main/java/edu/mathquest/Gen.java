package edu.mathquest;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/** Generates questions for each topic and level. */
final class Gen {
    private Gen() {}

    static Question next(Topic topic, int level, Random r) {
        // Now and then, review an easier level so earlier skills stay fresh.
        if (level > 1 && r.nextInt(10) < 3) level = rnd(r, 1, level - 1);
        return switch (topic) {
            case FRACTIONS -> switch (level) {
                case 1 -> fIdentify(r);
                case 2 -> fEquivalent(r);
                case 3 -> fCompare(r);
                case 4 -> fAddSub(r);
                default -> fTimesWhole(r);
            };
            case DECIMALS -> switch (level) {
                case 1 -> dTenths(r);
                case 2 -> dHundredths(r);
                case 3 -> dTenthsToHundredths(r);
                default -> dCompare(r);
            };
            case MULTIPLICATION -> switch (level) {
                case 1 -> mTable(r, new int[]{1, 2, 5, 10}, 1, 10);
                case 2 -> mTable(r, new int[]{3, 4}, 1, 10);
                case 3 -> mTable(r, new int[]{6, 7, 8, 9}, 2, 10);
                case 4 -> mTable(r, new int[]{2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12}, 2, 12);
                default -> mBig(r);
            };
        };
    }

    // ---------- helpers ----------

    static int rnd(Random r, int lo, int hi) {
        return lo + r.nextInt(hi - lo + 1);
    }

    static int gcd(int a, int b) {
        return b == 0 ? a : gcd(b, a % b);
    }

    static String f(int n, int d) {
        return n + "/" + d;
    }

    /** A bar of d parts with the first n coloured green. */
    static String bar(int n, int d) {
        return "§a" + "█".repeat(n) + "§7" + "█".repeat(d - n) + "§r";
    }

    /** A 10x10 grid with n squares shaded. */
    static String grid(int n) {
        StringBuilder sb = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            int shaded = Math.max(0, Math.min(10, n - row * 10));
            sb.append(bar(shaded, 10));
            if (row < 9) sb.append('\n');
        }
        return sb.toString();
    }

    static String hundredths(int n) {
        return String.format("0.%02d", n);
    }

    private static List<String> ints(int answer, int... candidates) {
        List<String> out = new ArrayList<>();
        for (int c : candidates) if (c >= 0 && c != answer) out.add(String.valueOf(c));
        return out;
    }

    private static String plural(int n, String word) {
        return n + " " + word + (n == 1 ? "" : "s");
    }

    // ---------- fractions ----------

    static Question fIdentify(Random r) {
        int d = rnd(r, 2, 8), n = rnd(r, 1, d - 1);
        String prompt = r.nextBoolean()
                ? "A cake is cut into " + d + " equal slices.\nYou eat " + plural(n, "slice") + ".\n\n"
                  + bar(n, d) + "\n\nWhat fraction of the cake did you eat?"
                : "What fraction of the bar is §agreen§r?\n\n" + bar(n, d);
        String explain = bar(n, d) + "\nThe §lbottom§r number (denominator) = how many equal parts in total: §e" + d
                + "§r\nThe §ltop§r number (numerator) = how many parts we count: §e" + n
                + "§r\nSo the answer is §a" + f(n, d) + "§r.";
        List<String> wrong = List.of(f(n, d - n), f(d - n, d), f(d, n), f(n + 1, d), f(n, d + 1));
        return Question.of(prompt, f(n, d), wrong, () -> f(rnd(r, 1, 7), rnd(r, 2, 9)), explain, true, r);
    }

    static Question fEquivalent(Random r) {
        int b = rnd(r, 2, 6), a;
        do a = rnd(r, 1, b - 1); while (gcd(a, b) != 1);
        int k = rnd(r, 2, 5);
        String explain = "The bottom went from " + b + " to " + (b * k) + " — that's §e×" + k + "§r.\n"
                + "Do the same to the top: " + a + " × " + k + " = " + (a * k) + ".\n"
                + "So §a" + f(a, b) + " = " + f(a * k, b * k) + "§r.\n\n"
                + "Multiplying the top AND bottom by the same number gives an equal fraction.";
        if (r.nextBoolean()) {
            String prompt = f(a, b) + " = ?/" + (b * k) + "\n\nWhat number goes where the §e?§r is?";
            return Question.of(prompt, String.valueOf(a * k),
                    ints(a * k, a, a * k + 1, a * k - 1, a + k, b * k - a),
                    () -> String.valueOf(rnd(r, 1, b * k)), explain, true, r);
        }
        String prompt = "Which fraction is §eequal§r to " + f(a, b) + "?";
        return Question.of(prompt, f(a * k, b * k),
                List.of(f(a + k, b + k), f(a * k, b), f(a, b * k), f(a * k + 1, b * k)),
                () -> f(rnd(r, 1, 9), rnd(r, 2, 12)), explain, true, r);
    }

    static Question fCompare(Random r) {
        String x, y, answer, explain;
        switch (r.nextInt(3)) {
            case 0 -> { // same denominator
                int d = rnd(r, 3, 12), p = rnd(r, 1, d - 1), q;
                do q = rnd(r, 1, d - 1); while (q == p);
                x = f(p, d);
                y = f(q, d);
                answer = p > q ? x : y;
                explain = "Both are cut into §e" + d + "§r equal parts, so the pieces are the same size.\n"
                        + "More pieces = bigger: " + Math.max(p, q) + " is more than " + Math.min(p, q)
                        + ".\nSo §a" + answer + "§r is bigger.";
            }
            case 1 -> { // same numerator
                int n = rnd(r, 1, 3), a = rnd(r, n + 1, 10), b;
                do b = rnd(r, n + 1, 10); while (b == a);
                x = f(n, a);
                y = f(n, b);
                answer = a < b ? x : y;
                int small = Math.min(a, b), big = Math.max(a, b);
                explain = "Both have §e" + n + "§r " + (n == 1 ? "piece" : "pieces") + ".\n"
                        + "But cutting into " + small + " parts makes §lbigger§r pieces than cutting into " + big + ".\n"
                        + "(1/" + small + " of a cake is more than 1/" + big + " of a cake!)\n"
                        + "So §a" + answer + "§r is bigger.";
            }
            default -> { // one denominator is a multiple of the other
                int b = rnd(r, 2, 5), k = rnd(r, 2, 3), big = b * k;
                int a = rnd(r, 1, b - 1), c = rnd(r, 1, big - 1);
                x = f(a, b);
                y = f(c, big);
                answer = a * k == c ? "They are equal" : (a * k > c ? x : y);
                explain = "Make the bottoms the same first: " + x + " = " + f(a * k, big) + " (×" + k + " top and bottom).\n"
                        + "Now compare " + f(a * k, big) + " with " + y + ": "
                        + (a * k == c ? "they are the same!" : "§a" + answer + "§r is bigger.");
            }
        }
        String prompt = "Which is §ebigger§r?\n\n      " + x + "   or   " + y;
        return Question.of(prompt, answer, List.of(x, y, "They are equal"), null, explain, false, r);
    }

    static Question fAddSub(Random r) {
        int d = rnd(r, 3, 12);
        if (r.nextBoolean()) {
            int a = rnd(r, 1, d - 1), b = rnd(r, 1, d - a), s = a + b;
            String prompt = f(a, d) + " + " + f(b, d) + " = ?";
            String explain = "The bottoms are the same (" + d + "), so the pieces are the same size.\n"
                    + "Just add the tops: " + a + " + " + b + " = " + s + ".\n"
                    + "The bottom stays " + d + " → §a" + f(s, d) + "§r" + (s == d ? " (that's one whole!)" : "") + "\n\n"
                    + "§cDon't add the bottoms!§r " + f(s, d + d) + " would be wrong.";
            return Question.of(prompt, f(s, d),
                    List.of(f(s, d + d), f(s + 1, d), a != b ? f(Math.abs(a - b), d) : f(s, d + 1), f(s - 1, d)),
                    () -> f(rnd(r, 1, d), d), explain, true, r);
        }
        int a = rnd(r, 2, d), b = rnd(r, 1, a - 1), s = a - b;
        String prompt = f(a, d) + " − " + f(b, d) + " = ?";
        String explain = "The bottoms are the same (" + d + "), so just subtract the tops: "
                + a + " − " + b + " = " + s + ".\nThe bottom stays " + d + " → §a" + f(s, d) + "§r.";
        return Question.of(prompt, f(s, d),
                List.of(f(a + b, d), f(s + 1, d), s > 1 ? f(s - 1, d) : f(s, d + 1), f(s, d + d)),
                () -> f(rnd(r, 1, d), d), explain, true, r);
    }

    static Question fTimesWhole(Random r) {
        int w = rnd(r, 2, 5), b = rnd(r, 2, 10), a = rnd(r, 1, b - 1), top = w * a;
        String prompt = r.nextBoolean()
                ? w + " × " + f(a, b) + " = ?"
                : "Each day you eat " + f(a, b) + " of a pie.\nHow much pie do you eat in " + w + " days?";
        StringBuilder adds = new StringBuilder();
        for (int i = 0; i < w; i++) adds.append(i == 0 ? "" : " + ").append(f(a, b));
        String explain = w + " × " + f(a, b) + " means adding " + f(a, b) + " " + w + " times:\n"
                + adds + " = §a" + f(top, b) + "§r\n\n"
                + "Multiply only the §ltop§r by " + w + ". The bottom stays " + b + "."
                + (top > b ? "\n(That's more than 1 whole!)" : top == b ? "\n(That's exactly 1 whole!)" : "");
        return Question.of(prompt, f(top, b),
                List.of(f(top, w * b), f(a, w * b), f(w + a, b), f(top + 1, b)),
                () -> f(rnd(r, 1, 20), b), explain, true, r);
    }

    // ---------- tenths & hundredths ----------

    static Question dTenths(Random r) {
        int n = rnd(r, 1, 9);
        String explain = bar(n, 10) + "\nTenths are the §lfirst§r place after the dot.\n"
                + "§e" + n + "§r tenths = " + f(n, 10) + " = §a0." + n + "§r";
        List<String> wrongDec = List.of("0.0" + n, n + ".0", "0." + (10 - n), "1." + n);
        return switch (r.nextInt(3)) {
            case 0 -> Question.of("Write §e" + f(n, 10) + "§r as a decimal.", "0." + n, wrongDec,
                    () -> "0." + rnd(r, 1, 9), explain, true, r);
            case 1 -> Question.of("§e0." + n + "§r is the same as which fraction?", f(n, 10),
                    List.of(f(n, 100), f(10, n), n > 1 ? f(1, n) : f(n, 1), f(10 - n, 10)),
                    () -> f(rnd(r, 1, 9), 10), explain, true, r);
            default -> Question.of("The bar has 10 equal parts.\nWhat decimal is §agreen§r?\n\n" + bar(n, 10), "0." + n,
                    wrongDec, () -> "0." + rnd(r, 1, 9), explain, true, r);
        };
    }

    static Question dHundredths(Random r) {
        int n;
        if (r.nextInt(10) < 3) n = rnd(r, 1, 9);
        else do n = rnd(r, 11, 99); while (n % 10 == 0);
        String answer = hundredths(n);
        int rev = (n % 10) * 10 + n / 10;
        List<String> wrongDec = n < 10
                ? List.of("0." + n, n + ".0", "0." + n + "0", "0.00" + n)
                : List.of((n / 10) + "." + (n % 10), rev % 10 != 0 && rev != n ? hundredths(rev) : n + ".0", "0.0" + n, n + ".0");
        String explain = "Hundredths use §ltwo§r places after the dot (like cents in a dollar).\n"
                + f(n, 100) + " = §a" + answer + "§r"
                + (n < 10 ? "\n\nThere are 0 tenths and " + n + " hundredths, so we need a §e0§r first: 0.0" + n : "")
                + "\n\n" + answer + " is like " + n + " cents.";
        return switch (r.nextInt(3)) {
            case 0 -> Question.of("Write §e" + f(n, 100) + "§r as a decimal.", answer, wrongDec,
                    () -> hundredths(rnd(r, 1, 99)), explain, true, r);
            case 1 -> Question.of("§e" + answer + "§r is the same as which fraction?", f(n, 100),
                    List.of(f(n, 10), f(n, 1000), rev != n && rev % 10 != 0 ? f(rev, 100) : f(n + 1, 100)),
                    () -> f(rnd(r, 1, 99), 100), explain, true, r);
            default -> Question.of("The grid has 100 squares.\nWhat decimal is §agreen§r?\n\n" + grid(n), answer,
                    wrongDec, () -> hundredths(rnd(r, 1, 99)), explain, true, r);
        };
    }

    static Question dTenthsToHundredths(Random r) {
        int a = rnd(r, 1, 9);
        switch (r.nextInt(3)) {
            case 0 -> {
                String explain = "1 tenth = 10 hundredths.\nSo " + a + " tenths = " + a + " × 10 = §a" + (a * 10)
                        + "§r hundredths.\n" + f(a, 10) + " = " + f(a * 10, 100) + "\n\n" + bar(a, 10);
                return Question.of(f(a, 10) + " = ?/100\n\nWhat number goes where the §e?§r is?", String.valueOf(a * 10),
                        ints(a * 10, a, a * 100, a + 10, a * 10 + 1), () -> String.valueOf(rnd(r, 1, 99)), explain, true, r);
            }
            case 1 -> {
                int b = rnd(r, 1, 9), s = a * 10 + b;
                String explain = "Change the tenths into hundredths first:\n" + f(a, 10) + " = " + f(a * 10, 100) + "\n"
                        + "Then add the tops: " + f(a * 10, 100) + " + " + f(b, 100) + " = §a" + f(s, 100) + "§r\n\n"
                        + "§cDon't add the bottoms!§r";
                return Question.of(f(a, 10) + " + " + f(b, 100) + " = ?", f(s, 100),
                        List.of(f(a + b, 110), f(a + b, 100), f(a + b, 10), f(s, 110)),
                        () -> f(rnd(r, 1, 99), 100), explain, true, r);
            }
            default -> {
                String explain = "0." + a + " means " + a + " tenths.\n" + a + " tenths = " + (a * 10) + " hundredths.\n"
                        + "So 0." + a + " = 0." + a + "0 = §a" + f(a * 10, 100) + "§r\n\nAdding a 0 on the end doesn't change a decimal.";
                return Question.of("Which is §eequal§r to 0." + a + "?", f(a * 10, 100),
                        List.of(f(a, 100), "0.0" + a, f(a * 10, 10), a + ".0"),
                        () -> f(rnd(r, 1, 99), 100), explain, true, r);
            }
        }
    }

    static Question dCompare(Random r) {
        int t = rnd(r, 1, 9), h;
        if (r.nextInt(100) < 15) h = t * 10;
        else do h = rnd(r, 11, 99); while (h % 10 == 0);
        String x = "0." + t, y = hundredths(h);
        boolean flip = r.nextBoolean();
        String first = flip ? y : x, second = flip ? x : y;
        String answer = t * 10 == h ? "They are equal" : (t * 10 > h ? x : y);
        String explain = "Give them the same number of places: " + x + " = 0." + t + "0\n"
                + "Now compare " + (t * 10) + " hundredths with " + h + " hundredths:\n"
                + (t * 10 == h ? "§aThey are equal!§r" : "§a" + answer + "§r is bigger.")
                + "\n\nMore digits does §lnot§r mean bigger! Think of money: 0." + t + "0 is " + (t * 10) + " cents.";
        return Question.of("Which is §ebigger§r?\n\n      " + first + "   or   " + second, answer,
                List.of(first, second, "They are equal"), null, explain, false, r);
    }

    // ---------- multiplication ----------

    static Question mTable(Random r, int[] tables, int lo, int hi) {
        int a = tables[r.nextInt(tables.length)], b = rnd(r, lo, hi), ans = a * b;
        if (r.nextBoolean()) { int tmp = a; a = b; b = tmp; }
        String prompt = switch (r.nextInt(4)) {
            case 0 -> "You build " + a + " rows of " + b + " blocks.\nHow many blocks is that?";
            case 1 -> "A chest has " + a + " stacks with " + b + " diamonds each.\nHow many diamonds?";
            default -> a + " × " + b + " = ?";
        };
        return Question.of(prompt, String.valueOf(ans),
                ints(ans, ans + a, ans - a, ans + b, ans - b, a + b, ans + 1, ans + 10),
                () -> String.valueOf(rnd(r, 1, Math.max(12, ans + 12))), timesTip(a, b), true, r);
    }

    static String timesTip(int a, int b) {
        int ans = a * b;
        String eq = a + " × " + b + " = §a" + ans + "§r\n\n";
        int o = a == 10 || a == 9 || a == 5 || a == 2 || a == 4 || a == 11 || a == 1 ? b : a;
        int t = o == a ? b : a;
        return eq + switch (t) {
            case 1 -> "Anything × 1 stays the same.";
            case 10 -> "×10 trick: put a 0 on the end. " + o + " → " + ans;
            case 9 -> "×9 trick: do ×10, then take one away.\n10 × " + o + " = " + (10 * o) + ", and " + (10 * o) + " − " + o + " = " + ans;
            case 5 -> "×5 trick: do ×10, then cut it in half.\n10 × " + o + " = " + (10 * o) + ", half of that is " + ans;
            case 2 -> "×2 is doubling: " + o + " + " + o + " = " + ans;
            case 4 -> "×4 is double, then double again: " + o + " → " + (2 * o) + " → " + ans;
            case 11 -> o < 10 ? "×11 trick for one digit: write it twice! " + o + " → " + ans
                    : "×11: do ×10 then add one more " + o + ": " + (10 * o) + " + " + o + " = " + ans;
            default -> "Think of it as " + a + " groups of " + b + ".\nIf you know " + (a - 1) + " × " + b + " = " + ((a - 1) * b)
                    + ", add one more " + b + ": " + ((a - 1) * b) + " + " + b + " = " + ans;
        };
    }

    static Question mBig(Random r) {
        if (r.nextBoolean()) {
            int a;
            do a = rnd(r, 12, 49); while (a % 10 == 0);
            int b = rnd(r, 2, 9), ans = a * b, tens = a / 10 * 10, ones = a % 10;
            String explain = "Split " + a + " into " + tens + " + " + ones + ":\n"
                    + tens + " × " + b + " = " + (tens * b) + "\n"
                    + ones + " × " + b + " = " + (ones * b) + "\n"
                    + (tens * b) + " + " + (ones * b) + " = §a" + ans + "§r";
            return Question.of(a + " × " + b + " = ?", String.valueOf(ans),
                    ints(ans, tens * b + ones, ans + 10, ans - 10, ans + b, ans - b),
                    () -> String.valueOf(ans + rnd(r, -20, 20)), explain, true, r);
        }
        int b = rnd(r, 2, 10), f = rnd(r, 2, 10), p = b * f;
        String explain = "What times " + b + " makes " + p + "?\nCount by " + b + "s: "
                + countBy(b, f) + "\nThat's §a" + f + "§r jumps, so " + f + " × " + b + " = " + p + ".";
        return Question.of("? × " + b + " = " + p + "\n\nWhat number is the §e?§r", String.valueOf(f),
                ints(f, f + 1, f - 1, f + 2, p - b, b), () -> String.valueOf(rnd(r, 1, 12)), explain, true, r);
    }

    private static String countBy(int step, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i <= times; i++) sb.append(i == 1 ? "" : ", ").append(step * i);
        return sb.toString();
    }
}
