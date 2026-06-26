package nl.tudelft.instrumentation.concolic;

import java.util.*;
import java.util.stream.Collectors;

import com.microsoft.z3.*;

/**
 * You should write your solution using this class.
 * 
 * Z3 API: https://z3prover.github.io/api/html/classcom_1_1microsoft_1_1z3_1_1_context.html
 */
public class ConcolicExecutionLab {

    static long seed;
    static Random r;
    static String outputDir;
    static long durationMs;

    static {
        String seedProp = System.getProperty("concolic.seed");
        if (seedProp != null) {
            seed = Long.parseLong(seedProp);
        } else {
            seed = new Random().nextLong();
            System.out.println("Generated random seed: " + seed);
        }
        r = new Random(seed);

        outputDir = System.getProperty("concolic.output.dir", "concolic_results");

        try {
            durationMs = Long.parseLong(System.getProperty("concolic.duration", "300")) * 1000;
        } catch (NumberFormatException e) {
            durationMs = 300 * 1000;
        }
    }

    static Boolean isFinished = false;
    static List<String> currentTrace;
    static int traceLength = 10;

    static Queue<List<String>> inputQueue = new LinkedList<>();
    static Set<String> visitedBranches = new HashSet<>();
    static Set<String> seenTraces = new HashSet<>();
    static int pathHash = 0;

    static Set<String> uniqueBranches = new HashSet<>();
    static Set<String> uniqueErrors = new HashSet<>();
    static long startTime = System.currentTimeMillis();

    static List<double[]> branchConvergence = new ArrayList<>();
    static List<Object[]> errorConvergence = new ArrayList<>();

    static long lastSampleTime = 0;
    static final long SAMPLE_INTERVAL_MS = 5000;

    static int maxUniqueBranches = 0;
    static List<String> bestTrace = new ArrayList<>();
    static int problemNumber = -1;

    static void initialize(String[] inputSymbols){
        // Initialise a random trace from the input symbols of the problem.
        currentTrace = generateRandomTrace(inputSymbols);
    }

    static MyVar createVar(String name, Expr value, Sort s){
        Context c = PathTracker.ctx;
        /**
         * Create var, assign value and add to path constraint.
         * We show how to do it for creating new symbols, please
         * add similar steps to the functions below in order to
         * obtain a path constraint.
         */
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.addToModel(c.mkEq(z3var, value));
        return new MyVar(z3var, name);
    }

    static MyVar createInput(String name, Expr value, Sort s){
        // Create an input var, these should be free variables!
        Context c = PathTracker.ctx;

        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);

        // The following code is to add an additional constraint on the input variable.
        // The input variable must have a value that is equal to one of the input symbols.
        BoolExpr constraint = c.mkFalse();
        for (String input: PathTracker.inputSymbols) {
            constraint = c.mkOr(constraint, c.mkEq(z3var, c.mkString(input)));
        }

        PathTracker.addToModel(constraint);

        MyVar myVar = new MyVar(z3var, name);
        PathTracker.inputs.add(myVar);
        return myVar;
    }

    static MyVar createBoolExpr(BoolExpr var, String operator){
        // Handle the following unary operators: !
        if (operator.equals("!")) {
            return new MyVar(PathTracker.ctx.mkNot(var));
        }
        return new MyVar(var);
    }

    static MyVar createBoolExpr(BoolExpr left_var, BoolExpr right_var, String operator){
        // Handle the following binary operators: &, &&, |, ||
        Context c = PathTracker.ctx;
        switch (operator) {
            case "&":
            case "&&":
                return new MyVar(c.mkAnd(left_var, right_var));
            case "|":
            case "||":
                return new MyVar(c.mkOr(left_var, right_var));
            default:
                return new MyVar(c.mkFalse());
        }
    }

    static MyVar createIntExpr(IntExpr var, String operator){
        // Handle the following unary operators for numerical operations: +, -
        Context c = PathTracker.ctx;
        switch (operator) {
            case "+":
                return new MyVar(var);
            case "-":
                return new MyVar(c.mkUnaryMinus(var));
            default:
                return new MyVar(c.mkFalse());
        }
    }

    static MyVar createIntExpr(IntExpr left_var, IntExpr right_var, String operator){
        // Handle the following binary operators for numerical operations: +, -, /, *, %, ^, ==, <=, <, >= and >
        Context c = PathTracker.ctx;
        switch (operator) {
            case "+":
                return new MyVar(c.mkAdd(left_var, right_var));
            case "-":
                return new MyVar(c.mkSub(left_var, right_var));
            case "*":
                return new MyVar(c.mkMul(left_var, right_var));
            case "/":
                return new MyVar(c.mkDiv(left_var, right_var));
            case "%":
                return new MyVar(c.mkMod(left_var, right_var));
            case "^":
                return new MyVar(left_var);
            case "==":
                return new MyVar(c.mkEq(left_var, right_var));
            case "!=":
                return new MyVar(c.mkNot(c.mkEq(left_var, right_var)));
            case "<":
                return new MyVar(c.mkLt(left_var, right_var));
            case "<=":
                return new MyVar(c.mkLe(left_var, right_var));
            case ">":
                return new MyVar(c.mkGt(left_var, right_var));
            case ">=":
                return new MyVar(c.mkGe(left_var, right_var));
            default:
                return new MyVar(c.mkFalse());
        }
    }

    static MyVar createStringExpr(SeqExpr left_var, SeqExpr right_var, String operator){
        // We only support String.equals
        if (operator.equals("==")) {
            return new MyVar(PathTracker.ctx.mkEq(left_var, right_var));
        }
        return new MyVar(PathTracker.ctx.mkFalse());
    }

    static void assign(MyVar var, String name, Expr value, Sort s){
        // All variable assignments, use single static assignment
        Context c = PathTracker.ctx;
        Expr z3var = c.mkConst(c.mkSymbol(name + "_" + PathTracker.z3counter++), s);
        PathTracker.addToModel(c.mkEq(z3var, value));
        var.z3var = z3var;
    }

    static void encounteredNewBranch(MyVar condition, boolean value, int line_nr){
        // Call the solver
        String branchKey = line_nr + "_" + value + "_" + pathHash;
        String negatedKey = line_nr + "_" + !value + "_" + pathHash;

        uniqueBranches.add(line_nr + "_" + value);

        if (!visitedBranches.contains(negatedKey)) {
            BoolExpr condExpr = (BoolExpr) condition.z3var;
            BoolExpr negated = value ? PathTracker.ctx.mkNot(condExpr) : condExpr;
            PathTracker.solve(negated, false);
        }

        visitedBranches.add(branchKey);

        pathHash = 31 * pathHash + (line_nr * (value ? 1 : -1));

        BoolExpr condExpr = (BoolExpr) condition.z3var;
        BoolExpr actualBranch = value ? condExpr : PathTracker.ctx.mkNot(condExpr);
        PathTracker.addToBranches(actualBranch);
    }

    static void newSatisfiableInput(LinkedList<String> new_inputs) {
        // Hurray! found a new branch using these new inputs!
        // Remove the extra quotes from the inputs that were find by the solver.
        List<String> trimmed_new_inputs = new_inputs.stream()
                .map(s -> s.replaceAll("\"", ""))
                .collect(Collectors.toList());

        String traceKey = trimmed_new_inputs.toString();
        if (!seenTraces.contains(traceKey)) {
            seenTraces.add(traceKey);
            inputQueue.add(trimmed_new_inputs);
        }
    }

    /**
     * Method for fuzzing new inputs for a program.
     * @param inputSymbols the inputSymbols to fuzz from.
     * @return a fuzzed sequence
     */
    static List<String> fuzz(String[] inputSymbols){
        /*
         * Add here your code for fuzzing a new sequence for the RERS problem.
         * You can guide your fuzzer to fuzz "smart" input sequences to cover
         * more branches using concolic execution. Right now we just generate
         * a complete random sequence using the given input symbols. Please
         * change it to your own code.
         */
        if (!inputQueue.isEmpty()) {
            return inputQueue.poll();
        }
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

    static int detectProblemNumber() {
        try {
            return Integer.parseInt(System.getProperty("concolic.problem", "-1"));
        } catch (NumberFormatException e) {
            return -1;
        }
    }


    static void run() {
        initialize(PathTracker.inputSymbols);
        startTime = System.currentTimeMillis();
        lastSampleTime = startTime;
        problemNumber = detectProblemNumber();

        long timeLimitMs = durationMs;
        int maxIterations = 10000;
        int iteration = 0;

        System.out.println("Mode: concolic | Problem: " + problemNumber
                + " | Duration: " + (durationMs / 1000) + "s"
                + " | Seed: " + seed
                + " | Output: " + outputDir);

        while(!isFinished && iteration < maxIterations) {
            long now = System.currentTimeMillis();

            if (now - startTime > timeLimitMs) {
                break;
            }

            PathTracker.reset();
            pathHash = 0;

            int branchesBefore = uniqueBranches.size();
            currentTrace = fuzz(PathTracker.inputSymbols);
            PathTracker.runNextFuzzedSequence(currentTrace.toArray(new String[0]));
            int branchesThisTrace = uniqueBranches.size() - branchesBefore;

            if (branchesThisTrace > maxUniqueBranches) {
                maxUniqueBranches = branchesThisTrace;
                bestTrace = new ArrayList<>(currentTrace);
            }

            iteration++;

            now = System.currentTimeMillis();
            if (now - lastSampleTime >= SAMPLE_INTERVAL_MS) {
                double elapsed = (now - startTime) / 1000.0;
                branchConvergence.add(new double[]{elapsed, uniqueBranches.size()});
                lastSampleTime = now;
            }
        }

        double finalElapsed = (System.currentTimeMillis() - startTime) / 1000.0;
        branchConvergence.add(new double[]{finalElapsed, uniqueBranches.size()});

        writeConvergenceCSVs();

        List<String> sortedErrors = new ArrayList<>(uniqueErrors);
        Collections.sort(sortedErrors);

        System.out.println("=== Fuzzing Results ===");
        System.out.println("Mode: concolic");
        System.out.println("Total unique branches visited: " + uniqueBranches.size());
        System.out.println("Max unique branches in a single trace: " + maxUniqueBranches);
        System.out.println("Best trace: " + bestTrace);
        System.out.println("Triggered errors (" + uniqueErrors.size() + "): " + sortedErrors);
        System.out.println("Convergence CSVs: " + outputDir + "/problem" + problemNumber + "_concolic_*.csv");

        isFinished = true;
    }

    public static void output(String out){
        if (out.contains("error_")) {
            String errorCode = out.substring(out.indexOf("error_"));
            if (uniqueErrors.add(errorCode.trim())) {
                double elapsed = (System.currentTimeMillis() - startTime) / 1000.0;
                errorConvergence.add(new Object[]{elapsed, errorCode.trim()});
            }
        }
    }

    static void writeConvergenceCSVs() {
        try {
            String base = outputDir;
            new java.io.File(base).mkdirs();

            String prefix = base + "/problem" + problemNumber + "_concolic_";

            try (java.io.PrintWriter pw = new java.io.PrintWriter(prefix + "branches.csv")) {
                pw.println("elapsed_seconds,unique_branches");
                for (double[] row : branchConvergence) {
                    pw.printf("%.1f,%d%n", row[0], (long) row[1]);
                }
            }

            try (java.io.PrintWriter pw = new java.io.PrintWriter(prefix + "errors.csv")) {
                pw.println("elapsed_seconds,error_code");
                for (Object[] row : errorConvergence) {
                    pw.printf("%.1f,%s%n", (double) row[0], (String) row[1]);
                }
            }

        } catch (Exception e) {
            System.out.println("Failed to write CSV: " + e.getMessage());
        }
    }
}


