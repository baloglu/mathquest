package edu.mathquest;

import java.util.List;

import static edu.mathquest.Gen.bar;
import static edu.mathquest.Gen.grid;

/** Short lesson pages shown before practice. Each page is {title, body}. */
final class Lessons {
    private Lessons() {}

    static List<String[]> pages(Topic t) {
        return switch (t) {
            case FRACTIONS -> List.of(
                    new String[]{"What is a fraction?",
                            "A fraction is a §lpart of a whole§r.\n\n"
                                    + bar(3, 4) + "\n\n"
                                    + "This bar is cut into §e4§r equal parts. §a3§r are green.\n"
                                    + "So §a3/4§r (three-fourths) is green.\n\n"
                                    + "§eTop§r number = parts we count (numerator)\n"
                                    + "§eBottom§r number = total equal parts (denominator)"},
                    new String[]{"Equal fractions",
                            "Different fractions can be the §lsame amount§r:\n\n"
                                    + bar(1, 2) + "  = 1/2\n"
                                    + bar(2, 4) + "  = 2/4\n"
                                    + bar(4, 8) + "  = 4/8\n\n"
                                    + "Rule: multiply the top §land§r bottom by the same number.\n"
                                    + "1/2 → (×2) → 2/4 → (×2) → 4/8"},
                    new String[]{"Which is bigger?",
                            "§eSame bottom:§r compare the tops.\n3/8 < 5/8 because 5 pieces is more than 3.\n\n"
                                    + "§eSame top:§r the §lsmaller§r bottom wins!\n1/3 > 1/6 because cutting a cake into 3 gives bigger pieces than cutting it into 6.\n\n"
                                    + "§eDifferent bottoms:§r make them the same first.\n1/2 vs 3/8 → 1/2 = 4/8, and 4/8 > 3/8."},
                    new String[]{"Adding and subtracting",
                            "When the bottoms are the same, add or subtract §lonly the tops§r:\n\n"
                                    + "2/8 + 3/8 = §a5/8§r\n"
                                    + bar(2, 8) + " + " + bar(3, 8) + "\n= " + bar(5, 8) + "\n\n"
                                    + "7/10 − 4/10 = §a3/10§r\n\n"
                                    + "§cThe bottom stays the same!§r (2/8 + 3/8 is NOT 5/16)"},
                    new String[]{"Whole number × fraction",
                            "3 × 2/5 means 2/5 + 2/5 + 2/5.\n\n"
                                    + "Multiply only the top: 3 × 2 = 6\n"
                                    + "So 3 × 2/5 = §a6/5§r\n\n"
                                    + "6/5 is more than one whole (5/5 = 1)."});
            case DECIMALS -> List.of(
                    new String[]{"Tenths",
                            "Cut a whole into §e10§r equal parts. Each part is one §ltenth§r.\n\n"
                                    + bar(3, 10) + "\n\n"
                                    + "3 tenths = §a3/10§r = §a0.3§r\n\n"
                                    + "The first place after the dot is the §etenths§r place."},
                    new String[]{"Hundredths",
                            "Cut a whole into §e100§r parts. Each part is one §lhundredth§r.\n\n"
                                    + grid(25) + "\n\n"
                                    + "25 hundredths = §a25/100§r = §a0.25§r\n"
                                    + "It's like money: $0.25 is 25 cents out of 100!\n\n"
                                    + "Careful with small ones: 7/100 = §a0.07§r (need a 0 in the tenths place)."},
                    new String[]{"Tenths = hundredths",
                            "1 tenth is the same as 10 hundredths.\n\n"
                                    + "0.5 = 0.50 = 5/10 = §a50/100§r\n\n"
                                    + "Adding a 0 on the end doesn't change a decimal.\n\n"
                                    + "To add 3/10 + 25/100, change the tenths first:\n"
                                    + "3/10 = 30/100, so 30/100 + 25/100 = §a55/100§r = 0.55"},
                    new String[]{"Comparing decimals",
                            "Which is bigger, 0.5 or 0.45?\n\n"
                                    + "Give them the same number of places: 0.5 = 0.50\n"
                                    + "Now compare 50 hundredths with 45 hundredths.\n"
                                    + "§a0.5 is bigger!§r\n\n"
                                    + "More digits does §lnot§r mean bigger.\n50 cents is more than 45 cents."});
            case MULTIPLICATION -> List.of(
                    new String[]{"What is multiplying?",
                            "Multiplying is adding §lequal groups§r.\n\n"
                                    + "3 × 4 means 3 groups of 4:\n"
                                    + bar(4, 4) + "\n" + bar(4, 4) + "\n" + bar(4, 4) + "\n"
                                    + "4 + 4 + 4 = §a12§r\n\n"
                                    + "Order doesn't matter: 3 × 4 = 4 × 3"},
                    new String[]{"Handy tricks",
                            "§e×2§r  double it: 2 × 7 = 7 + 7 = 14\n"
                                    + "§e×4§r  double, then double again: 6 → 12 → 24\n"
                                    + "§e×5§r  do ×10, then half: 5 × 8 → 80 → 40\n"
                                    + "§e×9§r  do ×10, take one away: 9 × 6 → 60 − 6 = 54\n"
                                    + "§e×10§r  put a 0 on the end: 7 → 70\n"
                                    + "§e×11§r  one digit? write it twice: 11 × 4 = 44"},
                    new String[]{"Stuck? Build from what you know",
                            "Don't know 7 × 8?\n\n"
                                    + "You know 7 × 7 = 49.\n"
                                    + "7 × 8 is just one more 7: 49 + 7 = §a56§r\n\n"
                                    + "Or split it: 7 × 8 = 7 × 4 + 7 × 4 = 28 + 28 = §a56§r"},
                    new String[]{"Bigger numbers",
                            "23 × 4 → split 23 into 20 + 3:\n\n"
                                    + "20 × 4 = 80\n"
                                    + " 3 × 4 = 12\n"
                                    + "80 + 12 = §a92§r\n\n"
                                    + "Missing number: ? × 6 = 42\n"
                                    + "Count by 6s: 6, 12, 18, 24, 30, 36, 42 → that's §a7§r!"});
        };
    }
}
