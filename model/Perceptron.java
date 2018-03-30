package tagger.model;

import tagger.data.Sentence;
import tagger.data.Token;
import tagger.utility.Logger;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Robert Mchardy
 * @author Alexander Ehmann
 * A multi-class perceptron.
 */
public class Perceptron {

    private Weights weights;
    private final int numClasses;
    private final int numFeatures;
    private double learningRate = 0.1;

    /**
     * Constructs a multi-class perceptron classifier.
     *
     * @param numClasses  The number of classes.
     * @param numFeatures The number of total features
     */
    public Perceptron(int numClasses, int numFeatures) {
        this.numClasses = numClasses;
        this.numFeatures = numFeatures + 1;
        weights = new Weights(this.numClasses, this.numFeatures);
    }

    /**
     * Predicts the best fitting class for a token with the learned weights of the perceptron. Returns the ID of the
     * class and stores it in token.predictedLabelIndex.
     *
     * @param token The token for which the class will be predicted.
     * @return The id of the predicted class.
     */
    private int predict(Token token) {
        int id = -1;
        float bestScore = Float.NEGATIVE_INFINITY;
        // Argmax over all classes
        for (int i = 0; i < numClasses; i++) {
            float score = weights.score(i, token.features);
            if (score >= bestScore) {
                bestScore = score;
                id = i;
            }
        }
        token.predictedLabelIndex = id;
        token.prediction = LabelExtractor.getLabel(token.predictedLabelIndex);
        if (token.next != null) {
            FeatureExtractors.extractPreviousTag(token.next);
        }
        return id;
    }

    /**
     * Predicts the best fitting class for a token with the learned averaged weights of the perceptron. Returns the ID
     * of the class and stores it in token.predictedLabelIndex.
     *
     * @param token The token for which the class will be predicted.
     * @return The ID of the predicted class.
     */
    public int predictAverage(Token token) {
        int id = -1;
        float bestScore = Float.NEGATIVE_INFINITY;
        // Argmax over all classes.
        for (int i = 0; i < numClasses; i++) {
            float score = weights.scoreAverage(i, token.features);
            if (score >= bestScore) {
                bestScore = score;
                id = i;
            }
        }
        token.predictedLabelIndex = id;
        token.prediction = LabelExtractor.getLabel(token.predictedLabelIndex);
        if (token.next != null) {
            FeatureExtractors.extractPreviousTag(token.next);
        }
        return id;
    }

    /**
     * Adjusts the weights of the perceptron according to the training data.
     *
     * @param trainingData       The training sentences.
     * @param developmentData    The test sentences.
     * @param numberOfIterations How many times the model is trained on the data.
     */
    public void train(List<Sentence> trainingData, List<Sentence> developmentData, int numberOfIterations) {
        List<Sentence> trainData = new ArrayList<>(trainingData);
        for (int i = 0; i < numberOfIterations; i++) {
            Collections.shuffle(trainData);
            // Allows finer adjustments of the weights after a certain amount of training.
            if (i % 8 == 0 && i != 0) {
                learningRate *= 0.7f;
            }
            for (Sentence s : trainData) {
                Collections.shuffle(s);
                for (Token t : s) {
                    predict(t);
                    // Only adjust the weights if the prediction is wrong.
                    if (t.correctLabelIndex != t.predictedLabelIndex) {
                        weights.update(t.predictedLabelIndex, t.correctLabelIndex, t.features, learningRate);
                    }
                    weights.incrementCounter();
                }
            }
        }
        weights.normalizeAverage();
        // If test sentences are given, annotate them.
        if (developmentData != null) {
            for (Sentence s : developmentData) {
                predictAverage(s);
            }
        }
    }

    /**
     * Predicts the classes for all tokens of a sentence.
     *
     * @param sentence The given sentence.
     */
    private void predict(Sentence sentence) {
        sentence.forEach(this::predict);
    }

    /**
     * Predicts the classes for all tokens of a sentence.
     *
     * @param sentence The given sentence.
     */
    public void predictAverage(Sentence sentence) {
        sentence.forEach(this::predictAverage);
    }

    /**
     * Saves the learned weights to a file.
     *
     * @param filepath The file where the weights will be stored.
     */
    public void saveWeights(String filepath) {
        try (FileWriter fw = new FileWriter(filepath); BufferedWriter buff = new BufferedWriter(fw)) {
            for (int i = 0; i < numClasses; i++) {
                buff.write(LabelExtractor.getLabel(i));
                for (int j = 0; j < numFeatures; j++) {
                    buff.write(String.format(" %f", weights.getWeight(i, j)));
                }
                buff.write("\n");
            }
        } catch (IOException e) {
            Logger.printException(e);
        }
    }
}
