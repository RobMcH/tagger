package tagger.model;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * <p>
 * Stores the feature weights for a binary perceptron.
 */
public class BinaryWeights {

    private float[] weights;
    private int numFeatures;

    /**
     * Constructs a one-dimensional weight-array for the given number of features and a bias.
     *
     * @param numFeatures The number of features.
     */
    public BinaryWeights(int numFeatures) {
        this.numFeatures = numFeatures + 1;
        weights = new float[this.numFeatures];
        // Set the bias to 1.0f.
        weights[0] = 1.0f;
        for (int i = 1; i < numFeatures; i++) {
            // Set every normal feature weight to 0.0f.
            weights[i] = 0.0f;
        }
    }

    /**
     * Updates the weights for the features for a wrong prediction.
     *
     * @param features     The feature vector of a token.
     * @param learningRate The learning rate for the adjustment of the weights.
     */
    public void update(int[] features, double learningRate) {
        // Adjust bias.
        weights[0] += (float) learningRate;
        // Adjust weights for the features.
        for (int target : features) {
            if (target < weights.length - 1) {
                weights[target + 1] += (float) learningRate;
            }
        }
    }

    /**
     * Scores a feature vector according to the current weights.
     *
     * @param featureVector The feature vector of a token.
     * @return The score for the binary perceptron given this feature vector.
     */
    public float score(int[] featureVector) {
        float result = weights[0];
        for (int target : featureVector) {
            if (target < weights.length - 1) {
                result += weights[target + 1];
            }
        }
        return result;
    }
}
