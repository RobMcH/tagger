package tagger.data;

import tagger.utility.BidirectionalMap;
import tagger.utility.Logger;
import tagger.utility.MutableInt;

import java.util.*;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Calculates a confusion matrix for a given list of sentences.
 */
public class ConfusionMatrix {
    private final int[][] matrix;
    private final BidirectionalMap<String, Integer> indexMap;

    /**
     * Used for ordering the labels according to their error frequency.
     */
    private static <K, V extends Comparable<? super V>>
    SortedSet<Map.Entry<K, V>> entriesSortedByValues(Map<K, V> map) {
        SortedSet<Map.Entry<K, V>> sortedEntries = new TreeSet<>(
                (e1, e2) -> {
                    int res = e2.getValue().compareTo(e1.getValue());
                    return res != 0 ? res : 1;
                }
        );
        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

    /**
     * Construct a confusion matrix for a given list of sentences.
     *
     * @param data The list of sentences.
     */
    public ConfusionMatrix(List<Sentence> data) {
        TreeMap<String, MutableInt> labelFreq = new TreeMap<>();
        // Count how many errors are made per label.
        MutableInt count;
        for (Sentence s : data) {
            for (Token t : s) {
                count = labelFreq.get(t.label);
                if (count == null) {
                    labelFreq.put(t.label, new MutableInt());
                } else {
                    count.increment();
                }
                count = labelFreq.get(t.prediction);
                if (count == null) {
                    labelFreq.put(t.prediction, new MutableInt());
                } else {
                    count.increment();
                }
            }
        }
        // Create the matrix.
        matrix = new int[labelFreq.size()][];
        indexMap = new BidirectionalMap<>(labelFreq.size());
        for (int i = 0; i < labelFreq.size(); i++) {
            matrix[i] = new int[labelFreq.size()];
        }
        // Create a list of labels sorted by error frequency.
        int i = 0;
        for (Map.Entry<String, MutableInt> e : entriesSortedByValues(labelFreq)) {
            if (i < labelFreq.size()) {
                indexMap.put(e.getKey(), i++);
            }
        }
        // Fill the matrix.
        for (Sentence s : data) {
            for (Token t : s) {
                matrix[indexMap.keyMapGet(t.label)][indexMap.keyMapGet(t.prediction)] += 1;
            }
        }
    }

    /**
     * Calculate the number of errors for the given gold and predicted label according to the matrix.
     *
     * @param goldLabel The given gold label.
     * @param predLabel The given predicted label.
     * @return The number of errors.
     */
    public int numberErrors(String goldLabel, String predLabel) {
        return matrix[indexMap.keyMapGet(goldLabel)][indexMap.keyMapGet(predLabel)];
    }

    /**
     * Prints the confusion matrix. The printed matrix is sorted such that the most frequent errors are in the upper
     * left corner.
     *
     * @param maxDim The maximum dimensions of the printed matrix.
     */
    public void print(int maxDim) {
        Logger.printString("", 5);
        for (int i = 0; i <= maxDim; i++) {
            Logger.printString(indexMap.valueMapGet(i), 6);
        }
        Logger.printString("\n", 1);
        for (int j = 0; j <= maxDim; j++) {
            Logger.printString(indexMap.valueMapGet(j), 5);
            for (int i = 0; i <= maxDim; i++) {
                Logger.printString(Integer.toString(matrix[j][i]), 5);
                if (i < maxDim) {
                    Logger.printString(" ");
                } else {
                    Logger.printString("\n");
                }
            }
        }
    }
}
