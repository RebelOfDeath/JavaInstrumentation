package nl.tudelft.instrumentation.fuzzing;

import java.util.*;
import java.util.Random;
import java.io.*;

/**
 * You should write your own solution using this class.
 */
public class FuzzingLab {
        static final long   SEED;
        static Random r;
        static {
                String seedProp = System.getProperty("fuzzing.seed");
                if (seedProp != null) {
                        SEED = Long.parseLong(seedProp);
                } else {
                        SEED = new Random().nextLong();
                }
                r = new Random(SEED);
        }
        static List<String> currentTrace;
        static int traceLength = 10;
        static boolean isFinished = false;

        // ── Configuration (pass via -Dfuzzing.X=Y on the java command line) ──────
        static final String MODE       = System.getProperty("fuzzing.mode",     "random");
        static final int    MUTATIONS  = Integer.parseInt(System.getProperty("fuzzing.mutations", "5"));
        static final String OUTPUT_DIR = System.getProperty("fuzzing.output.dir", "fuzzing_results");
        static final String PROBLEM_ID = System.getProperty("fuzzing.problem",  "unknown");
        static final long   DURATION_MS = Long.parseLong(System.getProperty("fuzzing.duration", "300")) * 1000L;

        // ── Branch tracking ───────────────────────────────────────────────────────
        static Set<String> allVisitedBranches  = new HashSet<>();
        static Set<String> currentRunBranches  = new HashSet<>();
        static int         maxUniqueBranchesInRun = 0;
        static List<String> bestTrace          = new ArrayList<>();

        // Accumulated branch distance for the trace currently being executed.
        // Reset to 0 at the start of each executeTrace() call.
        static double currentRunDistance = 0.0;

        // ── Error tracking ────────────────────────────────────────────────────────
        static Set<String> triggeredErrors = new TreeSet<>();

        // ── Seed pool for improved hill climber ─────────────────────────────────
        static final int SEED_POOL_SIZE = 20;
        static final int MIN_TRACE_LEN  = 5;
        static final int MAX_TRACE_LEN  = 20;
        static final int MAX_TRACE_LEN_MUTATED = 30;
        static final double RANDOM_EXPLORATION_RATIO = 0.20;
        static List<List<String>> seedPool = new ArrayList<>();
        // Parallel list: number of unique branches each seed trace covered on its own
        static List<Integer> seedPoolScores = new ArrayList<>();

        // ── Convergence data ──────────────────────────────────────────────────────
        // Branch convergence: periodic snapshots of {elapsed_ms, unique_branch_count}
        static List<long[]>   branchConvergence = new ArrayList<>();
        // Error convergence: one entry per newly found error {elapsed_ms, error_code}
        static List<Object[]> errorConvergence  = new ArrayList<>();

        static long startTime         = 0;
        static long lastBranchSample  = 0;
        static final long BRANCH_SAMPLE_INTERVAL_MS = 5_000; // snapshot every 5 s

        // ─────────────────────────────────────────────────────────────────────────

        static void initialize(String[] inputSymbols) {
                currentTrace = generateRandomTrace(inputSymbols);
        }

        /**
         * Called by the instrumentation on every if-statement encountered.
         * Tracks the branch and accumulates branch distance for the current trace.
         */
        static void encounteredNewBranch(MyVar condition, boolean value, int line_nr) {
                String key = line_nr + ":" + value;
                currentRunBranches.add(key);
                // Set.add() returns true only when the element was not already present.
                // Only accumulate distance for branches not yet globally visited:
                // summing over already-seen branches creates a fitness landscape where
                // "re-do all the easy stuff" scores better than "reach something new",
                // causing the hill climber to get stuck immediately.
                boolean isNew = allVisitedBranches.add(key);
                if (isNew) {
                        currentRunDistance += branchDistance(condition, !value);
                }
        }

        // ── Branch distance computation ───────────────────────────────────────────

        static double branchDistance(MyVar condition, boolean target) {
                if (condition == null) return 0.0;
                switch (condition.type) {
                        case BOOL:
                                return (condition.value == target) ? 0.0 : 1.0;
                        case UNARY:
                                if ("!".equals(condition.operator))
                                        return branchDistance(condition.left, !target);
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
                        case "&&": case "&":
                                return target
                                        ? branchDistance(condition.left, true)  + branchDistance(condition.right, true)
                                        : Math.min(branchDistance(condition.left, false), branchDistance(condition.right, false));
                        case "||": case "|":
                                return target
                                        ? Math.min(branchDistance(condition.left, true), branchDistance(condition.right, true))
                                        : branchDistance(condition.left, false) + branchDistance(condition.right, false);
                        case "^":
                                return target
                                        ? Math.min(branchDistance(condition.left, true)  + branchDistance(condition.right, false),
                                                   branchDistance(condition.left, false) + branchDistance(condition.right, true))
                                        : Math.min(branchDistance(condition.left, true)  + branchDistance(condition.right, true),
                                                   branchDistance(condition.left, false) + branchDistance(condition.right, false));
                        default:
                                return comparisonDistance(condition, target);
                }
        }

        static double comparisonDistance(MyVar condition, boolean target) {
                final int K = 1;
                int left  = condition.left.int_value;
                int right = condition.right.int_value;
                double raw;
                if (target) {
                        switch (condition.operator) {
                                case "==": raw = (left == right) ? 0 : Math.abs(left - right); break;
                                case "!=": raw = (left != right) ? 0 : 1;                      break;
                                case "<":  raw = (left <  right) ? 0 : (left - right + K);     break;
                                case "<=": raw = (left <= right) ? 0 : (left - right);         break;
                                case ">":  raw = (left >  right) ? 0 : (right - left + K);     break;
                                case ">=": raw = (left >= right) ? 0 : (right - left);         break;
                                default:   return 0.0;
                        }
                } else {
                        switch (condition.operator) {
                                case "==": raw = (left != right) ? 0 : 1;                      break;
                                case "!=": raw = (left == right) ? 0 : Math.abs(left - right); break;
                                case "<":  raw = (left >= right) ? 0 : (right - left);         break;
                                case "<=": raw = (left >  right) ? 0 : (right - left + K);     break;
                                case ">":  raw = (left <= right) ? 0 : (left - right);         break;
                                case ">=": raw = (left <  right) ? 0 : (left - right + K);     break;
                                default:   return 0.0;
                        }
                }
                return normalize(raw);
        }

        static double normalize(double d) { return d / (d + 1.0); }

        // ── Mutation operators (hill climber) ─────────────────────────────────────

        /**
         * Produces one mutated copy of the given trace by randomly applying one of:
         *   0 – change a random symbol
         *   1 – insert a random symbol at a random position
         *   2 – delete a random symbol
         */
        static List<String> mutate(List<String> trace, String[] symbols) {
                List<String> m = new ArrayList<>(trace);
                switch (r.nextInt(3)) {
                        case 0:
                                if (!m.isEmpty())
                                        m.set(r.nextInt(m.size()), symbols[r.nextInt(symbols.length)]);
                                break;
                        case 1:
                                m.add(r.nextInt(m.size() + 1), symbols[r.nextInt(symbols.length)]);
                                break;
                        case 2:
                                if (!m.isEmpty())
                                        m.remove(r.nextInt(m.size()));
                                break;
                }
                return m;
        }

        // ── Trace execution helper ────────────────────────────────────────────────

        /**
         * Resets per-run state, executes the trace through the instrumented problem,
         * updates convergence snapshots, and returns the total branch distance sum.
         */
        static double executeTrace(List<String> trace) {
                currentRunBranches  = new HashSet<>();
                currentRunDistance  = 0.0;
                DistanceTracker.runNextFuzzedSequence(trace.toArray(new String[0]));

                // Update best-trace record
                if (currentRunBranches.size() > maxUniqueBranchesInRun) {
                        maxUniqueBranchesInRun = currentRunBranches.size();
                        bestTrace = new ArrayList<>(trace);
                }

                // Periodic branch-count snapshot for convergence graph
                long now = System.currentTimeMillis();
                if (now - lastBranchSample >= BRANCH_SAMPLE_INTERVAL_MS) {
                        branchConvergence.add(new long[]{now - startTime, allVisitedBranches.size()});
                        lastBranchSample = now;
                }

                return currentRunDistance;
        }

        static List<String> generateRandomTrace(String[] symbols) {
                ArrayList<String> trace = new ArrayList<>();
                for (int i = 0; i < traceLength; i++)
                        trace.add(symbols[r.nextInt(symbols.length)]);
                return trace;
        }

        // ── Main loop ─────────────────────────────────────────────────────────────

        static void run() {
                initialize(DistanceTracker.inputSymbols);
                startTime        = System.currentTimeMillis();
                lastBranchSample = startTime;
                long endTime     = startTime + DURATION_MS;

                System.out.println("Mode: " + MODE + " | Problem: " + PROBLEM_ID
                        + " | Duration: " + (DURATION_MS / 1000) + "s"
                        + " | Mutations per step: " + MUTATIONS
                        + " | Seed: " + SEED);

                if ("smart".equals(MODE))
                        runHillClimber(endTime);
                else if ("improved".equals(MODE))
                        runImproved(endTime);
                else
                        runRandom(endTime);

                // Final branch snapshot
                branchConvergence.add(new long[]{System.currentTimeMillis() - startTime,
                        allVisitedBranches.size()});

                printResults();
                writeConvergenceCSVs();
                isFinished = true;
        }

        static void runRandom(long endTime) {
                executeTrace(currentTrace);
                while (!isFinished && System.currentTimeMillis() < endTime)
                        executeTrace(generateRandomTrace(DistanceTracker.inputSymbols));
        }

        /**
         * Hill-climber (Task 2):
         *  1. Execute the current best trace → get its branch-distance sum.
         *  2. Try MUTATIONS random mutations; execute each.
         *  3. If any mutation lowers the sum, adopt it as the new current best.
         *  4. Otherwise restart from a fresh random trace.
         */
        /**
         * Hill-climber (Task 2):
         *  1. Execute the current best trace → fitness = sum of distances for GLOBALLY NEW branches only.
         *  2. Try MUTATIONS random mutations; execute each.
         *  3. If any mutation lowers the fitness, adopt it as the new current best.
         *  4. If none improves, restart from a fresh random trace (no extra execution —
         *     the restart trace is evaluated as the first mutation of the next step).
         *
         * Fitness counts only unseen branches so the climber is always pulled toward
         * new program regions rather than re-optimising already-covered paths.
         */
        static void runHillClimber(long endTime) {
                String[] symbols  = DistanceTracker.inputSymbols;
                List<String> best = new ArrayList<>(currentTrace);
                double bestDist   = executeTrace(best);

                while (!isFinished && System.currentTimeMillis() < endTime) {
                        List<String> bestMutation = null;
                        double       bestMutDist  = bestDist;

                        for (int i = 0; i < MUTATIONS; i++) {
                                List<String> candidate = mutate(best, symbols);
                                double dist = executeTrace(candidate);
                                if (dist < bestMutDist) {
                                        bestMutDist  = dist;
                                        bestMutation = candidate;
                                }
                        }

                        if (bestMutation != null) {
                                // Improvement found — move to the better trace
                                best     = bestMutation;
                                bestDist = bestMutDist;
                        } else {
                                // Local minimum (all reachable new branches already found from
                                // this neighbourhood) — restart without an extra execution cost.
                                best     = generateRandomTrace(symbols);
                                bestDist = Double.MAX_VALUE;
                        }
                }
        }

        // ── Improved hill climber helpers ────────────────────────────────────────

        /**
         * Generates a random trace with length uniformly chosen between
         * MIN_TRACE_LEN and MAX_TRACE_LEN (inclusive).
         */
        static List<String> generateRandomTraceVariable(String[] symbols) {
                int len = MIN_TRACE_LEN + r.nextInt(MAX_TRACE_LEN - MIN_TRACE_LEN + 1);
                ArrayList<String> trace = new ArrayList<>();
                for (int i = 0; i < len; i++)
                        trace.add(symbols[r.nextInt(symbols.length)]);
                return trace;
        }

        /**
         * Adds a trace to the seed pool, scored by the number of unique branches
         * it covered in its run.  The pool is kept at most SEED_POOL_SIZE entries;
         * the weakest entry is evicted when the pool is full.
         */
        static void offerToSeedPool(List<String> trace, int branchCount) {
                if (seedPool.size() < SEED_POOL_SIZE) {
                        seedPool.add(new ArrayList<>(trace));
                        seedPoolScores.add(branchCount);
                } else {
                        // Find the index of the weakest seed
                        int worstIdx = 0;
                        int worstScore = seedPoolScores.get(0);
                        for (int i = 1; i < seedPoolScores.size(); i++) {
                                if (seedPoolScores.get(i) < worstScore) {
                                        worstScore = seedPoolScores.get(i);
                                        worstIdx = i;
                                }
                        }
                        if (branchCount > worstScore) {
                                seedPool.set(worstIdx, new ArrayList<>(trace));
                                seedPoolScores.set(worstIdx, branchCount);
                        }
                }
        }

        /**
         * Picks a random trace from the seed pool.  Returns null if the pool is empty.
         */
        static List<String> pickFromSeedPool() {
                if (seedPool.isEmpty()) return null;
                return new ArrayList<>(seedPool.get(r.nextInt(seedPool.size())));
        }

        /**
         * Splice mutation: takes two traces from the seed pool and combines them
         * by taking a prefix of one and a suffix of the other.
         * Falls back to a regular mutate if the pool has fewer than 2 entries.
         */
        static List<String> spliceMutation(String[] symbols) {
                if (seedPool.size() < 2) return null;
                List<String> a = seedPool.get(r.nextInt(seedPool.size()));
                List<String> b = seedPool.get(r.nextInt(seedPool.size()));
                int cutA = a.isEmpty() ? 0 : r.nextInt(a.size());
                int cutB = b.isEmpty() ? 0 : r.nextInt(b.size());
                List<String> spliced = new ArrayList<>(a.subList(0, cutA));
                spliced.addAll(b.subList(cutB, b.size()));
                // Enforce max length
                if (spliced.size() > MAX_TRACE_LEN_MUTATED) {
                        spliced = new ArrayList<>(spliced.subList(0, MAX_TRACE_LEN_MUTATED));
                }
                if (spliced.isEmpty()) {
                        spliced.add(symbols[r.nextInt(symbols.length)]);
                }
                return spliced;
        }

        /**
         * Improved hill climber (Task 3):
         *  - Maintains a seed pool of the top-K best traces by individual branch coverage.
         *  - Uses adaptive trace lengths (5–20 initially, mutations can grow up to 30).
         *  - 20% of the time does pure random exploration, 80% does hill climbing.
         *  - On restart after local minimum, picks from the seed pool and mutates
         *    instead of generating a fully random trace.
         *  - Uses 10 mutations per step (configurable via MUTATIONS property).
         *  - Includes a splice mutation that combines two seed pool traces.
         */
        static void runImproved(long endTime) {
                String[] symbols = DistanceTracker.inputSymbols;

                // Start with the initial trace
                List<String> best = new ArrayList<>(currentTrace);
                double bestDist   = executeTrace(best);
                offerToSeedPool(best, currentRunBranches.size());

                while (!isFinished && System.currentTimeMillis() < endTime) {

                        // ── 20% random exploration phase ──────────────────────────
                        if (r.nextDouble() < RANDOM_EXPLORATION_RATIO) {
                                List<String> randomTrace = generateRandomTraceVariable(symbols);
                                executeTrace(randomTrace);
                                offerToSeedPool(randomTrace, currentRunBranches.size());
                                continue;
                        }

                        // ── 80% hill climbing phase ───────────────────────────────
                        List<String> bestMutation = null;
                        double       bestMutDist  = bestDist;

                        for (int i = 0; i < MUTATIONS; i++) {
                                List<String> candidate;

                                // 1-in-MUTATIONS chance: try a splice mutation instead of a point mutation
                                if (i == 0 && seedPool.size() >= 2) {
                                        candidate = spliceMutation(symbols);
                                } else {
                                        candidate = mutate(best, symbols);
                                }

                                // Enforce max trace length for mutations
                                if (candidate.size() > MAX_TRACE_LEN_MUTATED) {
                                        candidate = new ArrayList<>(candidate.subList(0, MAX_TRACE_LEN_MUTATED));
                                }

                                double dist = executeTrace(candidate);
                                offerToSeedPool(candidate, currentRunBranches.size());

                                if (dist < bestMutDist) {
                                        bestMutDist  = dist;
                                        bestMutation = candidate;
                                }
                        }

                        if (bestMutation != null) {
                                // Improvement found — adopt the better trace
                                best     = bestMutation;
                                bestDist = bestMutDist;
                        } else {
                                // Local minimum — restart from the seed pool (or random if pool is empty)
                                List<String> seed = pickFromSeedPool();
                                if (seed != null) {
                                        // Mutate a seed pool entry to explore its neighbourhood
                                        best = mutate(seed, symbols);
                                } else {
                                        best = generateRandomTraceVariable(symbols);
                                }
                                bestDist = Double.MAX_VALUE;
                        }
                }
        }

        // ── Output, error tracking & convergence ──────────────────────────────────

        public static void output(String out) {
                // Errors surface as "Invalid input: error_N"
                // (IllegalStateException from Errors.__VERIFIER_error caught in the problem's call()).
                // All other automaton output symbols are suppressed.
                int idx = out.indexOf("error_");
                if (idx != -1) {
                        String code = out.substring(idx);
                        if (triggeredErrors.add(code)) {
                                // First time we see this error — record for convergence graph
                                errorConvergence.add(new Object[]{System.currentTimeMillis() - startTime, code});
                        }
                }
        }

        static void printResults() {
                System.out.println("=== Fuzzing Results ===");
                System.out.println("Mode: " + MODE);
                System.out.println("Total unique branches visited: " + allVisitedBranches.size());
                System.out.println("Max unique branches in a single trace: " + maxUniqueBranchesInRun);
                System.out.println("Best trace: " + bestTrace);
                System.out.println("Triggered errors (" + triggeredErrors.size() + "): " + triggeredErrors);
        }

        /**
         * Writes two CSV files to OUTPUT_DIR for later plotting:
         *   problem{N}_{mode}_branches.csv  – branch coverage over time
         *   problem{N}_{mode}_errors.csv    – each error code with the time it was first triggered
         */
        static void writeConvergenceCSVs() {
                new File(OUTPUT_DIR).mkdirs();
                String base = OUTPUT_DIR + "/problem" + PROBLEM_ID + "_" + MODE;
                try {
                        try (PrintWriter pw = new PrintWriter(new FileWriter(base + "_branches.csv"))) {
                                pw.println("elapsed_seconds,unique_branches");
                                for (long[] p : branchConvergence)
                                        pw.printf("%.1f,%d%n", p[0] / 1000.0, p[1]);
                        }
                        try (PrintWriter pw = new PrintWriter(new FileWriter(base + "_errors.csv"))) {
                                pw.println("elapsed_seconds,error_code");
                                for (Object[] e : errorConvergence)
                                        pw.printf("%.1f,%s%n", ((Long) e[0]) / 1000.0, e[1]);
                        }
                        System.out.println("Convergence CSVs: " + base + "_*.csv");
                } catch (IOException e) {
                        System.err.println("Warning: could not write convergence CSVs: " + e.getMessage());
                }
        }
}
