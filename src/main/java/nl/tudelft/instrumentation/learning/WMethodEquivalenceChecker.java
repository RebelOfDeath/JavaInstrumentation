package nl.tudelft.instrumentation.learning;

import java.util.*;

public class WMethodEquivalenceChecker extends EquivalenceChecker {

    private int w;
    private AccessSequenceGenerator accessSequenceGenerator;
    private DistinguishingSequenceGenerator distinguishingSequenceGenerator;

    public WMethodEquivalenceChecker(SystemUnderLearn sul, String[] inputSymbols, int w, DistinguishingSequenceGenerator dg, AccessSequenceGenerator ag) {
        super(sul, inputSymbols);
        this.w = w;
        this.distinguishingSequenceGenerator = dg;
        this.accessSequenceGenerator = ag;
    }

    @Override
    public Optional<Word<String>> verify(MealyMachine hypothesis) {
        List<Word<String>> accessSequences = accessSequenceGenerator.getAccessSequences();
        List<Word<String>> distinguishingSequences = distinguishingSequenceGenerator.getDistinguishingSequences();

        // Generate all middle parts X of length 0..w over inputSymbols
        List<Word<String>> middleParts = new ArrayList<>();
        generateWords(middleParts, new Word<>(), 0, w);

        // For each combination A · X · W, compare hypothesis output to SUL output
        for (Word<String> access : accessSequences) {
            for (Word<String> middle : middleParts) {
                for (Word<String> dist : distinguishingSequences) {
                    Word<String> testWord = access.append(middle).append(dist);
                    List<String> testList = testWord.asList();
                    if (testList.isEmpty()) continue;

                    String[] testArray = testList.toArray(new String[0]);
                    String[] hypOutput = hypothesis.getOutput(testArray);
                    String[] sulOutput = sul.getOutput(testArray);

                    for (int i = 0; i < testArray.length; i++) {
                        if (!hypOutput[i].equals(sulOutput[i])) {
                            // Return the prefix up to and including the first mismatch
                            return Optional.of(new Word<>(testList.subList(0, i + 1)));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Generate all words of length 0 to maxLen over inputSymbols.
     */
    private void generateWords(List<Word<String>> result, Word<String> current, int depth, int maxLen) {
        result.add(current);
        if (depth < maxLen) {
            for (String sym : inputSymbols) {
                generateWords(result, current.append(sym), depth + 1, maxLen);
            }
        }
    }
}
