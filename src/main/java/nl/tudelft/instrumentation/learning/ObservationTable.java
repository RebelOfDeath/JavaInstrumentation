package nl.tudelft.instrumentation.learning;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

/**
 * @author Bram Verboom
 *         The observation of the L* algorithm for learning mealy machines.
 */

public class ObservationTable implements DistinguishingSequenceGenerator, AccessSequenceGenerator {

    public static Word<String> word;

    private static final String SEPERATOR = ",";
    private static final String LAMBDA = " ";
    private static final Word<String> EMPTY = new Word<>();

    private String[] inputSymbols;

    private List<Word<String>> S;
    private List<Word<String>> E;

    // The actual observations: a map with (S u S A) as keys where each value
    // (row) represents the observations corresponding to (S u S A) E.
    private Map<Word<String>, ArrayList<String>> table;
    private SystemUnderLearn sul;

    public ObservationTable(String[] inputSymbols, SystemUnderLearn sul) {
        this.sul = sul;
        this.S = new ArrayList<>();
        this.E = new ArrayList<>();
        this.inputSymbols = inputSymbols;
        table = new HashMap<>();
        this.addToS(EMPTY);
        for (String s : this.inputSymbols) {
            Word<String> sym = new Word<>(s);
            this.addToE(sym);
        }
    }

    /**
     * Method that is used for checking whether the observation table is closed
     *
     * You should write your own logic here.
     *
     * @return an Optional.empty() if the table is consistent, or an Optional.of(_)
     *         with something usefull to extend the observation table with.
     */
    public Optional<Word<String>> checkForClosed() {
        // For each row in S·A, check if there exists a row in S with the same observations.
        // If not, the table is not closed: return that S·A element to promote to S.
        for (Word<String> s : S) {
            for (String a : inputSymbols) {
                Word<String> sa = s.append(a);
                ArrayList<String> saRow = table.get(sa);
                boolean found = false;
                for (Word<String> s2 : S) {
                    ArrayList<String> s2Row = table.get(s2);
                    if (saRow.equals(s2Row)) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    return Optional.of(sa);
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Method that is used for checking whether the observation table is consistent
     *
     * You should write your own logic here.
     *
     * @return an Optional.empty() if the table is consistent, or an Optional.of(_)
     *         with something usefull to extend the observation table with.
     */
    public Optional<Word<String>> checkForConsistent() {
        // For all s1, s2 in S where row(s1) == row(s2), check that for all a in inputSymbols:
        // row(s1·a) == row(s2·a). If not, find the distinguishing suffix e in E such that
        // the outputs for s1·a·e and s2·a·e differ, and return a·e to add to E.
        for (int i = 0; i < S.size(); i++) {
            for (int j = i + 1; j < S.size(); j++) {
                Word<String> s1 = S.get(i);
                Word<String> s2 = S.get(j);
                ArrayList<String> row1 = table.get(s1);
                ArrayList<String> row2 = table.get(s2);
                if (!row1.equals(row2)) {
                    continue;
                }
                // Rows are equal, so for each input symbol check that s1·a and s2·a also have equal rows
                for (String a : inputSymbols) {
                    Word<String> s1a = s1.append(a);
                    Word<String> s2a = s2.append(a);
                    ArrayList<String> row1a = table.get(s1a);
                    ArrayList<String> row2a = table.get(s2a);
                    if (!row1a.equals(row2a)) {
                        // Find the distinguishing suffix
                        for (int k = 0; k < E.size(); k++) {
                            if (!row1a.get(k).equals(row2a.get(k))) {
                                Word<String> ae = new Word<>(a).append(E.get(k));
                                return Optional.of(ae);
                            }
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    private String getResultFromSul(Word<String> trace) {
        String res = sul.getLastOutput(trace);
        // System.out.printf("Output for trace %s is %s\n", trace, res);
        return res;
    }

    /**
     * Method that is used for adding a new prefix to S
     * 
     * @param prefix the prefix to add to S, must be a list of symbols in the
     *               alphabet
     */
    public void addToS(Word<String> prefix) {
        System.out.printf("Adding %s to S\n", prefix);
        if (!S.contains(prefix)) {
            S.add(prefix);
            addRow(prefix);
            for (String symbol : inputSymbols) {
                addRow(prefix.append(symbol));
            }
        }
    }

    /**
     * Method that is used for adding a new suffix to E
     *
     * @param suffix the suffix to add to E, must be a list of symbols in the
     *               alphabet
     */
    public void addToE(Word<String> suffix) {
        System.out.printf("Adding %s to E\n", suffix);
        if (!E.contains(suffix)) {
            E.add(suffix);
            for (Entry<Word<String>, ArrayList<String>> entry : table.entrySet()) {
                Word<String> joined = entry.getKey().append(suffix);
                entry.getValue().add(getResultFromSul(joined));
            }
        }
    }

    /**
     * Method for adding a row to the observation table.
     *
     * Adds a row to the observation table and fills it with the correct
     * observations.
     */
    private void addRow(Word<String> base) {

        if (table.containsKey(base)) {
            return;
        } else {
            ArrayList<String> row = new ArrayList<>();
            for (Word<String> e : E) {
                row.add(getResultFromSul(base.append(e)));
            }
            table.put(base, row);
        }
    }

    private String rowToKey(ArrayList<String> input) {
        return String.join(",", input);
    }

    /**
     * Method to generate a {@link MealyMachine} from this observation table.
     *
     * Note: in order to generate a MealyMachine the observation table must be
     * consistent and closed. For any inconstistencies the model will create dummy
     * states.
     *
     * @return an MealyMachine that reflects the observations
     */
    public MealyMachine generateHypothesis() {
        Map<String, MealyState> states = new HashMap<>();
        int numStates = 0;
        for (Word<String> s : S) {
            String key = rowToKey(table.get(s));
            if (!states.containsKey(key)) {
                states.put(key, new MealyState(String.format("s%s", numStates++)));
            }
        }
        for (Word<String> s : S) {
            ArrayList<String> baseRow = table.get(s);
            MealyState from = states.get(rowToKey(baseRow));
            int index_in_alphabet = 0;
            for (String sym : inputSymbols) {
                Word<String> base = s.append(sym);
                ArrayList<String> row = table.get(base);
                String toKey = rowToKey(row);
                String output = baseRow.get(index_in_alphabet++);

                if (states.containsKey(toKey)) {
                    MealyTransition newTransition = new MealyTransition(output, states.get(toKey));
                    MealyTransition t = from.next(sym);
                    if (t != null && !t.equals(newTransition)) {
                        // assert false: "Observation table is not consistent";
                        MealyState unknownState = new MealyState(String.format("\"s%s?\"", numStates++));
                        newTransition = new MealyTransition(output, unknownState);
                        for (String sym2 : inputSymbols) {
                            unknownState.addEdge(sym2, new MealyTransition("?", unknownState));
                        }
                        from.addEdge(sym, new MealyTransition("?", unknownState));
                    } else {
                        from.addEdge(sym, newTransition);
                    }
                } else {
                    // assert false : "Observation table is not closed";
                    MealyState unknownState = new MealyState(String.format("\"s%s?\"", numStates++));
                    MealyTransition newTransition = new MealyTransition(output, unknownState);
                    for (String sym2 : inputSymbols) {
                        MealyTransition newTransition2 = new MealyTransition("?", unknownState);
                        unknownState.addEdge(sym2, newTransition2);
                    }
                    from.addEdge(sym, newTransition);
                }
            }
        }
        MealyState initialState = states.get(rowToKey(table.get(EMPTY)));
        return new MealyMachine(initialState);
    }

    public static String pretty(ArrayList<String> trace) {
        if (trace.size() == 0) {
            return LAMBDA;
        }
        return String.join(SEPERATOR, trace);
    }

    /**
     * Method to print the observation table in a nice way.
     */
    public void print() {
        ArrayList<ArrayList<String>> rows = new ArrayList<>();
        ArrayList<String> header = new ArrayList<>();
        rows.add(null);
        header.add("E");
        header.add(" ");
        for (Word<String> e : E) {
            header.add(e.toString());
        }
        rows.add(header);
        rows.add(null);
        int index = 0;
        for (Word<String> s : S) {
            ArrayList<String> row = new ArrayList<>();
            row.add(index++ == 0 ? "S" : "");
            row.add(s.toString());
            row.addAll(table.get(s));
            rows.add(row);
        }
        rows.add(null);
        index = 0;
        for (Word<String> s : S) {
            for (String symbol : inputSymbols) {
                Word<String> joined = s.append(symbol);
                ArrayList<String> row = new ArrayList<>();
                row.add(index++ == 0 ? "SA" : "");
                row.add(joined.toString());
                row.addAll(table.get(joined));
                rows.add(row);
            }
        }
        rows.add(null);
        int[] minSizes = new int[rows.get(1).size()];
        Arrays.fill(minSizes, 1);
        for (ArrayList<String> row : rows) {
            if (row == null) {
                continue;
            }
            for (int i = 0; i < minSizes.length; i++) {
                minSizes[i] = Math.max(minSizes[i], row.get(i).length() + 1);
            }
        }
        String empty = "-";
        for (int i = 0; i < minSizes.length; i++) {
            for (int j = 0; j < minSizes[i] + 1; j++) {
                empty += "-";
            }
            empty += "-";
        }
        for (ArrayList<String> row : rows) {
            if (row == null) {
                System.out.println(empty);
                continue;
            }
            for (int i = 0; i < minSizes.length; i++) {
                int l = minSizes[i];
                String f = "%-" + l + "s";
                row.set(i, String.format(f, row.get(i)));
            }
            System.out.printf("| %s|\n", String.join("| ", row));
        }
    }

    @Override
    public List<Word<String>> getAccessSequences() {
        return new ArrayList<>(S);
    }

    @Override
    public List<Word<String>> getDistinguishingSequences() {
        return new ArrayList<>(E);
    }

}
