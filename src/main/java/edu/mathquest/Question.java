package edu.mathquest;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Random;
import java.util.function.Supplier;

record Question(String prompt, List<String> choices, int correct, String explanation) {

    String answer() {
        return choices.get(correct);
    }

    /**
     * Builds a multiple-choice question from the right answer and candidate wrong answers.
     * Duplicates are dropped; the filler tops the list up to four choices.
     */
    static Question of(String prompt, String answer, List<String> wrong, Supplier<String> filler,
                       String explanation, boolean shuffle, Random r) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        set.add(answer);
        for (String w : wrong) {
            if (set.size() >= 4) break;
            if (w != null) set.add(w);
        }
        for (int tries = 0; set.size() < 4 && filler != null && tries < 50; tries++) {
            set.add(filler.get());
        }
        List<String> choices = new ArrayList<>(set);
        if (shuffle) Collections.shuffle(choices, r);
        return new Question(prompt, List.copyOf(choices), choices.indexOf(answer), explanation);
    }
}
