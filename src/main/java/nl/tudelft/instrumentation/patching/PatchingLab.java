package nl.tudelft.instrumentation.patching;
import java.util.*;
import java.io.*;

public class PatchingLab {

        static Random r = new Random();
        static boolean isFinished = false;

        static final int POPULATION_SIZE = 30;
        static final int MAX_GENERATIONS = 1000;
        static final int TOURNAMENT_SIZE = 3;
        static final double MUTATION_RATE = Double.parseDouble(System.getProperty("patching.mutationRate", "0.4"));
        static final long MAX_TIME_MS = Long.parseLong(System.getProperty("patching.duration", "300")) * 1000;

        static final String[] INT_OPERATORS = {"==", "!=", "<", ">", "<=", ">="};

        static String[][] population;
        static double[] fitnessValues;
        static String[] originalOperators;
        static int numOperators;
        static int totalTestsCount;
        static int totalPassedCount;
        static int totalFailedCount;

        static Set<Integer> operatorsHitInCurrentTest = new HashSet<>();
        static int[] passedCount;
        static int[] failedCount;
        static double[] tarantulaScores;

        static List<double[]> convergenceLog = new ArrayList<>();

        static void initialize(){
                numOperators = OperatorTracker.operators.length;
                originalOperators = Arrays.copyOf(OperatorTracker.operators, numOperators);

                population = new String[POPULATION_SIZE][numOperators];
                fitnessValues = new double[POPULATION_SIZE];
                passedCount = new int[numOperators];
                failedCount = new int[numOperators];
                tarantulaScores = new double[numOperators];

                population[0] = Arrays.copyOf(originalOperators, numOperators);
                for (int i = 1; i < POPULATION_SIZE; i++) {
                        population[i] = Arrays.copyOf(originalOperators, numOperators);
                        for (int j = 0; j < numOperators; j++) {
                                if (r.nextDouble() < 0.05) {
                                        population[i][j] = INT_OPERATORS[r.nextInt(INT_OPERATORS.length)];
                                }
                        }
                }
        }

        static boolean encounteredOperator(String operator, int left, int right, int operator_nr){
                operatorsHitInCurrentTest.add(operator_nr);

                String replacement = OperatorTracker.operators[operator_nr];
                if(replacement.equals("!=")) return left != right;
                if(replacement.equals("==")) return left == right;
                if(replacement.equals("<")) return left < right;
                if(replacement.equals(">")) return left > right;
                if(replacement.equals("<=")) return left <= right;
                if(replacement.equals(">=")) return left >= right;
                return false;
        }

        static boolean encounteredOperator(String operator, boolean left, boolean right, int operator_nr){
                operatorsHitInCurrentTest.add(operator_nr);

                String replacement = OperatorTracker.operators[operator_nr];
                if(replacement.equals("!=")) return left != right;
                if(replacement.equals("==")) return left == right;
                return false;
        }

        static double evaluateFitness(String[] candidate, boolean updateTarantula) {
                System.arraycopy(candidate, 0, OperatorTracker.operators, 0, numOperators);

                int passed = 0;

                for (int t = 0; t < totalTestsCount; t++) {
                        operatorsHitInCurrentTest.clear();
                        boolean result = OperatorTracker.runTest(t);

                        if (updateTarantula) {
                                for (int op : operatorsHitInCurrentTest) {
                                        if (result) passedCount[op]++;
                                        else failedCount[op]++;
                                }
                        }

                        if (result) passed++;
                }

                if (updateTarantula) {
                        totalPassedCount = passed;
                        totalFailedCount = totalTestsCount - passed;
                }

                return (double) passed / totalTestsCount;
        }

        static void computeTarantulaScores() {
                if (totalFailedCount == 0) {
                        Arrays.fill(tarantulaScores, 0.0);
                        return;
                }
                if (totalPassedCount == 0) {
                        Arrays.fill(tarantulaScores, 1.0);
                        return;
                }

                for (int i = 0; i < numOperators; i++) {
                        double failRatio = (double) failedCount[i] / totalFailedCount;
                        double passRatio = (double) passedCount[i] / totalPassedCount;
                        double denom = failRatio + passRatio;
                        tarantulaScores[i] = (denom == 0.0) ? 0.0 : failRatio / denom;
                }
        }

        static void resetTarantulaCounters() {
                Arrays.fill(passedCount, 0);
                Arrays.fill(failedCount, 0);
        }

        static int tournamentSelect() {
                int best = r.nextInt(POPULATION_SIZE);
                for (int i = 1; i < TOURNAMENT_SIZE; i++) {
                        int contender = r.nextInt(POPULATION_SIZE);
                        if (fitnessValues[contender] > fitnessValues[best]) best = contender;
                }
                return best;
        }

        static String[] mutate(String[] parent) {
                String[] child = Arrays.copyOf(parent, numOperators);
                int numToMutate = 1 + r.nextInt(3);
                for (int k = 0; k < numToMutate; k++) {
                        int targetIdx = -1;
                        if (r.nextDouble() < 0.7) {
                                for (int attempt = 0; attempt < 50; attempt++) {
                                        int i = r.nextInt(numOperators);
                                        if (tarantulaScores[i] > r.nextDouble() * 0.5) {
                                                targetIdx = i;
                                                break;
                                        }
                                }
                        }
                        if (targetIdx == -1) targetIdx = r.nextInt(numOperators);
                        child[targetIdx] = INT_OPERATORS[r.nextInt(INT_OPERATORS.length)];
                }
                return child;
        }

        static String[] crossover(String[] parent1, String[] parent2) {
                String[] child = new String[numOperators];
                int crossPoint = r.nextInt(numOperators);
                for (int i = 0; i < numOperators; i++) {
                        child[i] = (i < crossPoint) ? parent1[i] : parent2[i];
                }
                return child;
        }

        static void storePatch(String[] patch, double fitness, int generation) {
                String problemName = OperatorTracker.problem.getClass().getSimpleName();
                String filename = "patching_results_" + problemName + ".txt";
                try (PrintWriter pw = new PrintWriter(new FileWriter(filename))) {
                        pw.println("Problem: " + problemName);
                        pw.println("Generation: " + generation);
                        pw.println("Fitness: " + fitness);
                        pw.println("Operators (" + patch.length + "):");
                        for (int i = 0; i < patch.length; i++) {
                                pw.println("  [" + i + "] " + originalOperators[i] + " -> " + patch[i]);
                        }
                        pw.println();
                        pw.println("Convergence (generation, bestFitness):");
                        for (double[] entry : convergenceLog) {
                                pw.printf("  %d, %.4f%n", (int) entry[0], entry[1]);
                        }
                } catch (IOException e) {
                        System.err.println("Failed to write patch file: " + e.getMessage());
                }
        }

        static int getBestIndex() {
                int best = 0;
                for (int i = 1; i < POPULATION_SIZE; i++) {
                        if (fitnessValues[i] > fitnessValues[best]) best = i;
                }
                return best;
        }

        static void run() {
                initialize();
                totalTestsCount = OperatorTracker.tests.size();

                String problemName = OperatorTracker.problem.getClass().getSimpleName();
                System.out.println("=== Patching EA started for " + problemName + " ===");
                System.out.println("  Population: " + POPULATION_SIZE + " | Generations: " + MAX_GENERATIONS
                        + " | Mutation: " + MUTATION_RATE);
                System.out.println("  Operators: " + numOperators + " | Tests: " + OperatorTracker.tests.size());

                long startTime = System.currentTimeMillis();

                // Initial evaluation
                resetTarantulaCounters();
                fitnessValues[0] = evaluateFitness(population[0], true);
                computeTarantulaScores();

                // Now evaluate the rest of the initial population (no Tarantula update needed)
                for (int i = 1; i < POPULATION_SIZE; i++) {
                        fitnessValues[i] = evaluateFitness(population[i], false);
                }

                int bestIdx = getBestIndex();
                System.out.println("  Initial best fitness: " + String.format("%.4f", fitnessValues[bestIdx]));
                System.out.println("  Baseline (buggy) fitness: " + String.format("%.4f", fitnessValues[0]));
                convergenceLog.add(new double[]{0, fitnessValues[bestIdx]});

                int generation = 0;
                int staleCount = 0;
                double lastBest = fitnessValues[bestIdx];

                while (!isFinished) {
                        generation++;

                        if (fitnessValues[getBestIndex()] >= 1.0) {
                                System.out.println("  Perfect fitness at generation " + generation);
                                isFinished = true;
                                break;
                        }
                        if (generation >= MAX_GENERATIONS) {
                                System.out.println("  Max generations reached.");
                                isFinished = true;
                                break;
                        }
                        if (System.currentTimeMillis() - startTime > MAX_TIME_MS) {
                                System.out.println("  Time limit reached.");
                                isFinished = true;
                                break;
                        }

                        // Next generation
                        String[][] newPop = new String[POPULATION_SIZE][numOperators];

                        // Elitism
                        bestIdx = getBestIndex();
                        newPop[0] = Arrays.copyOf(population[bestIdx], numOperators);

                        for (int i = 1; i < POPULATION_SIZE; i++) {
                                int p1 = tournamentSelect();
                                int p2 = tournamentSelect();
                                String[] child;

                                if (r.nextDouble() < 0.5) {
                                        child = crossover(population[p1], population[p2]);
                                } else {
                                        child = Arrays.copyOf(population[p1], numOperators);
                                }

                                if (r.nextDouble() < MUTATION_RATE) {
                                        child = mutate(child);
                                }
                                newPop[i] = child;
                        }

                        population = newPop;

                        // Evaluate
                        resetTarantulaCounters();
                        fitnessValues[0] = evaluateFitness(population[0], true);
                        computeTarantulaScores();

                        for (int i = 1; i < POPULATION_SIZE; i++) {
                                fitnessValues[i] = evaluateFitness(population[i], false);
                        }

                        bestIdx = getBestIndex();
                        convergenceLog.add(new double[]{generation, fitnessValues[bestIdx]});

                        if (fitnessValues[bestIdx] > lastBest) {
                                lastBest = fitnessValues[bestIdx];
                                staleCount = 0;
                        } else {
                                staleCount++;
                        }

                        if (generation % 10 == 0 || fitnessValues[bestIdx] >= 1.0) {
                                long elapsed = (System.currentTimeMillis() - startTime) / 1000;
                                System.out.println("  Gen " + generation + " | Best: "
                                        + String.format("%.4f", fitnessValues[bestIdx])
                                        + " | " + elapsed + "s");
                        }
                }

                // Results
                bestIdx = getBestIndex();
                System.out.println("\n=== EA finished for " + problemName + " ===");
                System.out.println("  Best fitness: " + fitnessValues[bestIdx]);
                System.out.println("  Generations:  " + generation);

                int patchCount = 0;
                for (int i = 0; i < numOperators; i++) {
                        if (!population[bestIdx][i].equals(originalOperators[i])) {
                                System.out.println("  Patch: operator[" + i + "] "
                                        + originalOperators[i] + " -> " + population[bestIdx][i]);
                                patchCount++;
                        }
                }
                System.out.println("  Total operators patched: " + patchCount);

                storePatch(population[bestIdx], fitnessValues[bestIdx], generation);
                System.arraycopy(population[bestIdx], 0, OperatorTracker.operators, 0, numOperators);
        }

        public static void output(String out){
                // System.out.println(out);
        }
}