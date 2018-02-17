package tagger.model;

import tagger.data.Sentence;
import tagger.data.Token;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * A binary perceptron.
 */
public class BinaryPerceptron {

    private BinaryWeights weights;
    private int numFeatures;
    private double learningRate = 0.1;

    /**
     * Constructs a binary perceptron classifier.
     *
     * @param numFeatures The number of features.
     */
    public BinaryPerceptron(int numFeatures) {
        this.numFeatures = numFeatures;
        weights = new BinaryWeights(this.numFeatures);
    }


    /**
     * Predicts a class (0 or 1) for a given token according to the current weights.
     *
     * @param token The given token.
     * @return The predicted class.
     */
    public int predict(Token token) {
        token.predictedLabelIndex = weights.score(token.features) > 0.0f ? 1 : 0;
        return token.predictedLabelIndex;
    }


    /**
     * Adjust the weights of the binary perceptron according to the training data.
     *
     * @param trainingData       The training sentences.
     * @param developmentData
     * @param numberOfIterations How many times the model is trained on the data.
     */
    public void train(List<Sentence> trainingData, List<Sentence> developmentData, int numberOfIterations) {
        ArrayList<Sentence> train = new ArrayList<>(trainingData);
        for (int i = 0; i < numberOfIterations; i++) {
            Collections.shuffle(train);
            for (Sentence s : trainingData) {
                for (Token t : s) {
                    predict(t);
                    // Only adjust the weights if the prediction is wrong.
                    if (t.predictedLabelIndex > t.correctLabelIndex) {
                        weights.update(t.features, -learningRate);
                    } else if (t.predictedLabelIndex < t.correctLabelIndex) {
                        weights.update(t.features, learningRate);
                    }
                }
            }
        }
    }
}
