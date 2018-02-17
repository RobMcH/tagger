package tagger.model;

import tagger.data.Sentence;
import tagger.data.Token;

import java.util.List;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * <p>
 * Used for storing methods regarding the evaluation of the performance of the model.
 */
public class Evaluation {

    /**
     * Calculates the accuracy for a given data set. The accuracy is defined as
     * #correctPredictions / #totalPredictions
     *
     * @param data The list of sentences of which the accuracy will be calculated.
     * @return
     */
    public static double accuracy(List<Sentence> data) {
        double correctPredictions = 0.0;
        double totalPredictions = 0.0;
        for (Sentence s : data) {
            for (Token t : s) {
                if (t.label.equals(t.prediction)) {
                    correctPredictions++;
                }
            }
            totalPredictions += s.size();
        }
        return totalPredictions != 0.0 ? correctPredictions / totalPredictions : 0.0;
    }

    private Evaluation() {
    }
}