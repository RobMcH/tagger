package tagger.model;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Stores the perceptron's feature weights. The weights can automatically be adjusted for wrong predictions and
 * the class allows to score feature vectors and classes.
 */
public class Weights {
    private float[][] learningWeights;
    private float[][] averageWeights;
    private int numUpdates = 0;
    private final int numFeatures;
    private final int numClasses;

    /**
     * Constructs a two-dimensional weight-array for storing the weights for each feature and a bias for each class.
     *
     * @param numClasses  The amount of classes/labels.
     * @param numFeatures The amount of features.
     */
    public Weights(int numClasses, int numFeatures) {
        this.numFeatures = numFeatures;
        this.numClasses = numClasses;
        learningWeights = new float[this.numClasses][this.numFeatures];
        averageWeights = new float[this.numClasses][this.numFeatures];
        for (int i = 0; i < this.numClasses; i++) {
            for (int j = 0; j < this.numFeatures; j++) {
                // Initialize the averaged weights with zeros.
                averageWeights[i][j] = 0.0f;
                learningWeights[i][j] = 0.0f;
            }
        }
    }

    /**
     * Updates the weights for the features for wrong predictions.
     *
     * @param prediction        The prediction of the model.
     * @param correctLabelIndex The correct class.
     * @param features          The feature vector of the token.
     * @param learningRate      The learning rate for the adjustment of the weights.
     */
    public void update(int prediction, int correctLabelIndex, int[] features, double learningRate) {
        // Adjust bias.
        learningWeights[correctLabelIndex][0] += (float) learningRate;
        learningWeights[prediction][0] += (float) -learningRate;
        // Adjust weights for the features.
        for (int target : features) {
            if (target < learningWeights[correctLabelIndex].length - 1) {
                learningWeights[correctLabelIndex][target + 1] += (float) learningRate;
                learningWeights[prediction][target + 1] += (float) -learningRate;
            }
        }
    }

    /**
     * Scores a feature vector using the current weights.
     *
     * @param classId       The class the feature vector should be scored against.
     * @param featureVector The feature vector of the token.
     * @return The score for the class given the feature vector.
     */
    public float score(int classId, int[] featureVector) {
        float result = learningWeights[classId][0];
        for (int target : featureVector) {
            if (target < learningWeights[classId].length - 1) {
                result += learningWeights[classId][target + 1];
            }
        }
        return result;
    }

    /**
     * Returns the weight at the position weights[i][j].
     *
     * @param i The ID of the class.
     * @param j The ID of the feature.
     * @return the weight at weights[i][j].
     */
    public float getWeight(int i, int j) {
        if (i < learningWeights.length && j < learningWeights[0].length) {
            return learningWeights[i][j];
        } else {
            return 0.0f;
        }
    }

    /**
     * Updates the averaged weights with the latest weights.
     */
    public void updateAverage() {
        this.numUpdates++;
        for (int i = 0; i < this.numClasses; i++) {
            for (int j = 0; j < this.numFeatures; j++) {
                averageWeights[i][j] += learningWeights[i][j];
            }
        }
    }

    /**
     * Normalized the averaged weights with the number of updates made to them.
     */
    public void normalizeAverage() {
        for (int i = 0; i < this.numClasses; i++) {
            for (int j = 0; j < this.numFeatures; j++) {
                averageWeights[i][j] /= numUpdates;
            }
        }
    }

    /**
     * Scores a feature vector using the averaged weights.
     *
     * @param classId       The class the feature vector should be scored against.
     * @param featureVector The feature vector of the token.
     * @return The score for the class given the feature vector.
     */
    public float scoreAverage(int classId, int[] featureVector) {
        float result = averageWeights[classId][0];
        for (int target : featureVector) {
            if (target < averageWeights[classId].length - 1) {
                result += averageWeights[classId][target + 1];
            }
        }
        return result;
    }
}
