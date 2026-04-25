package nl.tudelft.instrumentation.fuzzing;

import java.util.*;
import java.util.Random;

/**
 * You should write your own solution using this class.
 */
public class FuzzingLab {
        static Random r = new Random();
        static List<String> currentTrace;
        static int traceLength = 10;
        static boolean isFinished = false;

        // All unique (line_nr:value) branches seen across all runs
        static Set<String> allVisitedBranches = new HashSet<>();

        // Branches seen in the current single trace execution
        static Set<String> currentRunBranches = new HashSet<>();

        // Best single-trace result for reporting
        static int maxUniqueBranchesInRun = 0;
        static List<String> bestTrace = new ArrayList<>();

        // All unique error codes triggered across all runs (e.g. "error_5")
        static Set<String> triggeredErrors = new TreeSet<>();

        static void initialize(String[] inputSymbols){
                currentTrace = generateRandomTrace(inputSymbols);
        }

        /**
         * Called every time an if-statement is reached during execution.
         * Tracks the branch and computes branch distance (distance to flip the branch).
         */
        static void encounteredNewBranch(MyVar condition, boolean value, int line_nr) {
                String branchKey = line_nr + ":" + value;
                currentRunBranches.add(branchKey);
                allVisitedBranches.add(branchKey);

                // Branch distance: how far are we from flipping this branch?
                // (i.e., making the condition evaluate to !value)
                double distance = branchDistance(condition, !value);
        }

        /**
         * Returns the normalized branch distance in [0,1] to make condition evaluate to target.
         * Uses the formulas from the branch distance lecture slides.
         */
        static double branchDistance(MyVar condition, boolean target) {
                if (condition == null) return 0.0;
                switch (condition.type) {
                        case BOOL:
                                return (condition.value == target) ? 0.0 : 1.0;
                        case UNARY:
                                if ("!".equals(condition.operator)) {
                                        return branchDistance(condition.left, !target);
                                }
                                return 0.0;
                        case BINARY:
                                return binaryBranchDistance(condition, target);
                        default:
                                return 0.0;
                }
        }

        static double binaryBranchDistance(MyVar condition, boolean target) {
                String op = condition.operator;
                switch (op) {
                        // Logical AND: p1 & p2 → true if both true, false if either false
                        case "&&":
                        case "&":
                                if (target) return branchDistance(condition.left, true) + branchDistance(condition.right, true);
                                else        return Math.min(branchDistance(condition.left, false), branchDistance(condition.right, false));

                        // Logical OR: p1 | p2 → true if either true, false if both false
                        case "||":
                        case "|":
                                if (target) return Math.min(branchDistance(condition.left, true), branchDistance(condition.right, true));
                                else        return branchDistance(condition.left, false) + branchDistance(condition.right, false);

                        // XOR: true if exactly one is true
                        case "^":
                                if (target) return Math.min(
                                        branchDistance(condition.left, true)  + branchDistance(condition.right, false),
                                        branchDistance(condition.left, false) + branchDistance(condition.right, true));
                                else        return Math.min(
                                        branchDistance(condition.left, true)  + branchDistance(condition.right, true),
                                        branchDistance(condition.left, false) + branchDistance(condition.right, false));

                        // Comparison operators on integers
                        default:
                                return comparisonDistance(condition, target);
                }
        }

        /**
         * Computes normalized branch distance for a comparison operator.
         * K=1 is used as a small constant to ensure distance > 0 when the boundary is tight.
         */
        static double comparisonDistance(MyVar condition, boolean target) {
                final int K = 1;
                int left  = condition.left.int_value;
                int right = condition.right.int_value;
                double raw;

                if (target) {
                        // Distance to make condition TRUE
                        switch (condition.operator) {
                                case "==": raw = (left == right) ? 0 : Math.abs(left - right); break;
                                case "!=": raw = (left != right) ? 0 : 1;                      break;
                                case "<":  raw = (left < right)  ? 0 : (left - right + K);     break;
                                case "<=": raw = (left <= right) ? 0 : (left - right);         break;
                                case ">":  raw = (left > right)  ? 0 : (right - left + K);     break;
                                case ">=": raw = (left >= right) ? 0 : (right - left);         break;
                                default:   return 0.0;
                        }
                } else {
                        // Distance to make condition FALSE (flip the operator)
                        switch (condition.operator) {
                                case "==": raw = (left != right) ? 0 : 1;                      break;
                                case "!=": raw = (left == right) ? 0 : Math.abs(left - right); break;
                                case "<":  raw = (left >= right) ? 0 : (right - left);         break;
                                case "<=": raw = (left > right)  ? 0 : (right - left + K);     break;
                                case ">":  raw = (left <= right) ? 0 : (left - right);         break;
                                case ">=": raw = (left < right)  ? 0 : (left - right + K);     break;
                                default:   return 0.0;
                        }
                }
                return normalize(raw);
        }

        /** Normalizes a raw distance value to [0, 1] using D = d/(d+1). */
        static double normalize(double d) {
                return d / (d + 1.0);
        }

        /**
         * Method for fuzzing new inputs for a program.
         * @param inputSymbols the inputSymbols to fuzz from.
         * @return a fuzzed sequence
         */
        static List<String> fuzz(String[] inputSymbols){
                return generateRandomTrace(inputSymbols);
        }

        /**
         * Generate a random trace from an array of symbols.
         * @param symbols the symbols from which a trace should be generated from.
         * @return a random trace that is generated from the given symbols.
         */
        static List<String> generateRandomTrace(String[] symbols) {
                ArrayList<String> trace = new ArrayList<>();
                for (int i = 0; i < traceLength; i++) {
                        trace.add(symbols[r.nextInt(symbols.length)]);
                }
                return trace;
        }

        /** Updates bestTrace if the current run covered more unique branches than any previous run. */
        static void updateBestTrace(List<String> trace) {
                if (currentRunBranches.size() > maxUniqueBranchesInRun) {
                        maxUniqueBranchesInRun = currentRunBranches.size();
                        bestTrace = new ArrayList<>(trace);
                }
        }

        static void run() {
                initialize(DistanceTracker.inputSymbols);

                long endTime = System.currentTimeMillis() + 5 * 60 * 1000L; // 5 minutes

                // Initial run
                currentRunBranches = new HashSet<>();
                DistanceTracker.runNextFuzzedSequence(currentTrace.toArray(new String[0]));
                updateBestTrace(currentTrace);

                while (!isFinished && System.currentTimeMillis() < endTime) {
                        List<String> trace = fuzz(DistanceTracker.inputSymbols);
                        currentTrace = trace;
                        currentRunBranches = new HashSet<>();
                        DistanceTracker.runNextFuzzedSequence(trace.toArray(new String[0]));
                        updateBestTrace(trace);
                }

                System.out.println("=== Fuzzing Results ===");
                System.out.println("Total unique branches visited: " + allVisitedBranches.size());
                System.out.println("Max unique branches in a single trace: " + maxUniqueBranchesInRun);
                System.out.println("Best trace: " + bestTrace);
                System.out.println("Triggered errors (" + triggeredErrors.size() + "): " + triggeredErrors);

                isFinished = true;
        }

        /**
         * Method that is used for catching the output from standard out.
         * You should write your own logic here.
         * @param out the string that has been outputted in the standard out.
         */
        public static void output(String out){
                // Errors arrive as "Invalid input: error_N" (IllegalStateException
                // from Errors.__VERIFIER_error caught in the instrumented call()).
                // Everything else is a normal automaton output symbol — suppress it.
                int idx = out.indexOf("error_");
                if (idx != -1) {
                        triggeredErrors.add(out.substring(idx));
                }
        }
}
