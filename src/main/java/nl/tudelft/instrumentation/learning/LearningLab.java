package nl.tudelft.instrumentation.learning;

import java.util.*;

/**
 * You should write your own solution using this class.
 */
public class LearningLab {
    static Random r = new Random();
    static int traceLength = 10;
    static boolean isFinished = false;

    static ObservationTable observationTable;
    static EquivalenceChecker equivalenceChecker;

    // W-method depth parameter (configurable via system property)
    static int wMethodDepth = Integer.getInteger("learning.wdepth", 3);
    static String dotFile = System.getProperty("learning.dotfile", "hypothesis.dot");

    static void run() {
        long startTime = System.currentTimeMillis();
        int membershipQueries = 0;

        SystemUnderLearn sul = new RersSUL();
        observationTable = new ObservationTable(LearningTracker.inputSymbols, sul);
        equivalenceChecker = new WMethodEquivalenceChecker(sul, LearningTracker.inputSymbols, wMethodDepth, observationTable, observationTable);

        // If requested, run LearnLib instead for comparison
        if (Boolean.getBoolean("learning.learnlib")) {
            LearnLibRunner llr = new LearnLibRunner();
            llr.start(wMethodDepth);
            return;
        }

        int round = 0;
        while (!isFinished) {
            round++;

            // Step 1: Make observation table closed and consistent
            boolean changed = true;
            while (changed) {
                changed = false;

                // Check for closedness
                Optional<Word<String>> notClosed = observationTable.checkForClosed();
                if (notClosed.isPresent()) {
                    System.out.printf("[Round %d] Table not closed, adding %s to S\n", round, notClosed.get());
                    observationTable.addToS(notClosed.get());
                    changed = true;
                    continue;
                }

                // Check for consistency
                Optional<Word<String>> notConsistent = observationTable.checkForConsistent();
                if (notConsistent.isPresent()) {
                    System.out.printf("[Round %d] Table not consistent, adding %s to E\n", round, notConsistent.get());
                    observationTable.addToE(notConsistent.get());
                    changed = true;
                }
            }

            // Step 2: Generate hypothesis
            MealyMachine hypothesis = observationTable.generateHypothesis();
            int numStates = hypothesis.getStates().length;
            long elapsed = System.currentTimeMillis() - startTime;
            System.out.printf("[Round %d] Hypothesis has %d states (elapsed: %d ms)\n", round, numStates, elapsed);

            // Save intermediate hypothesis
            hypothesis.writeToDot(dotFile);

            // Step 3: Check equivalence
            Optional<Word<String>> counterexample = equivalenceChecker.verify(hypothesis);

            if (counterexample.isPresent()) {
                Word<String> ce = counterexample.get();
                System.out.printf("[Round %d] Counterexample found: %s\n", round, ce);

                // Process counterexample: add all suffixes to E (Maler-Pnueli approach for Mealy machines)
                List<String> ceList = ce.asList();
                for (int i = 0; i < ceList.size(); i++) {
                    Word<String> suffix = new Word<>(ceList.subList(i, ceList.size()));
                    observationTable.addToE(suffix);
                }
            } else {
                // No counterexample found - learning is complete
                long totalTime = System.currentTimeMillis() - startTime;
                System.out.println("===========================================");
                System.out.printf("Learning complete! Final model has %d states\n", numStates);
                System.out.printf("Total rounds: %d\n", round);
                System.out.printf("Total time: %d ms\n", totalTime);
                System.out.println("===========================================");

                observationTable.print();
                hypothesis.writeToDot(dotFile);
                isFinished = true;
            }
        }
    }


    /**
     * Method that is used for catching the output from standard out.
     *
     * @param out the string that has been outputted in the standard out.
     */
    public static void output(String out) {
        // System.out.println(out);
    }
}
