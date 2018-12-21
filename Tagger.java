package tagger;

import tagger.data.ConfusionMatrix;
import tagger.data.Sentence;
import tagger.data.Token;
import tagger.model.Evaluation;
import tagger.model.FeatureExtractors;
import tagger.model.LabelExtractor;
import tagger.model.Perceptron;
import tagger.utility.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * The multi-class perceptron based Part-of-Speech tagger.
 */
public class Tagger {
    private static final HashSet<String> classCounter = new HashSet<>();
    private static int previousSentNr;

    /**
     * Returns the number of part of speech classes observed.
     *
     * @return number of POS classes.
     */
    public static int numClasses() {
        return classCounter.size();
    }

    /**
     * Clears the performance critical data structures of the tagger infrastructure. This should be performed if the
     * the tagger is run on different data sets in order to maintain the runtime performance.
     */
    public static void clear() {
        classCounter.clear();
        FeatureExtractors.stringMapper.clear();
        LabelExtractor.clear();
    }

    private Tagger() {
    }

    /**
     * Constructs a list of sentences from a file in the CoNLL format including information regarding the gold
     * and predicted labels.
     *
     * @param inputFile The name of the file in the CoNLL format.
     * @return The constructed list of sentences.
     */
    public static List<Sentence> readData(String inputFile) {
        LinkedList<Sentence> sentences = new LinkedList<>();
        try (FileReader fr = new FileReader(inputFile); BufferedReader buff = new BufferedReader(fr)) {
            String line;
            int sentNr = 0;
            previousSentNr = 1;
            boolean notAdded = false;
            Sentence sentence = new Sentence();
            String[] contents;
            while ((line = buff.readLine()) != null) {
                contents = line.split("\t");
                if (contents.length > 5 && contents[0].length() > 0) {
                    sentNr = getSentNumber(contents, sentNr);
                    Token token = constructToken(contents);
                    addTokenPOS(token);
                    notAdded = processToken(token, sentence, sentNr, sentences);
                }
            }
            if (notAdded) {
                sentences.add(sentence); // If the sentence was not added previously, add it now.
            }
        } catch (IOException e) {
            Logger.printException(e);
        }
        return sentences;
    }

    /**
     * Returns the sentence number (sequentially).
     */
    private static int getSentNumber(String[] contents, int sentNr) {
        if (contents[0].contains("_")) {
            return Integer.parseInt(contents[0].split("_")[0]);
        } else {
            if (Integer.parseInt(contents[0]) == 1) {
                return sentNr + 1;
            }
        }
        return -1;
    }

    /**
     * Constructs a new token for the given input.
     *
     * @return The constructed token.
     */
    private static Token constructToken(String[] contents) {
        Token token = new Token();
        token.word = contents[1];
        token.label = contents[4];
        token.prediction = contents[5];
        classCounter.add(token.label);
        classCounter.add(token.prediction);
        return token;
    }

    /**
     * Construct a list of determiners, proper nouns and adjectives as features.
     *
     * @param token The token whose POS will be added.
     */
    private static void addTokenPOS(Token token) {
        if (token.label.equals("DT")) {
            FeatureExtractors.addDeterminer(token.word);
        } else if (token.label.equals("JJ")) {
            FeatureExtractors.addAdjective(token.word);
        } else if (token.label.equals("NNP")) {
            FeatureExtractors.addProperNoun(token.word);
        }
    }

    /**
     * Processes a given token: The previous and next tokens are extracted and the token is added to the sentence.
     *
     * @return If the sentence was added to the sentences (meaning the token is EOS).
     */
    private static boolean processToken(Token token, Sentence sentence, int sentNr,
                                        List<Sentence> sentences) {
        FeatureExtractors.isEOSorBOS(sentence, token);
        if (previousSentNr == sentNr) {
            sentence.addToken(token);
            return true;
        } else {
            sentences.add(sentence);
            previousSentNr = sentNr;
            sentence = new Sentence();
            sentence.add(token);
            return false;
        }
    }

    /**
     * Extracts the three preceding and subsequent tokens for all tokens in the given list of sentences where the gold
     * and predicted label were confused.
     *
     * @param data      The list of sentences.
     * @param goldLabel The gold label.
     * @param predLabel The predicted label.
     */
    public static void extractInstances(List<Sentence> data, String goldLabel, String predLabel) {
        for (Sentence s : data) {
            for (int i = 0; i < s.size(); i++) {
                Token t = s.get(i);
                if (t.label.equals(goldLabel) && t.prediction.equals(predLabel)) {
                    for (int j = Math.max(0, i - 3); j < Math.min(s.size(), i + 4); j++) {
                        Token temp = s.get(j);
                        if (temp.equals(t)) {
                            Logger.printString(String.format("%-13s\t%-10s\t%-10s%n", "*" + temp.word + "*", temp.label,
                                    temp.prediction));
                        } else {
                            Logger.printString(String.format("%-15s\t%-10s\t%-10s%n", temp.word, temp.label,
                                    temp.prediction));
                        }
                    }
                    Logger.printString("**********************\n");
                }
            }
        }
    }

    /**
     * Saves the predictions for the given data in the given file. The format is:
     * <word> <label> <prediction> [*]
     * Where [] marks that the star is optional and only present if the prediction is wrong.
     *
     * @param data     A list of sentences.
     * @param filepath A path to a file where the predictions will be stored.
     */
    public static void savePredictions(List<Sentence> data, String filepath) {
        try (FileWriter fw = new FileWriter(filepath); BufferedWriter buff = new BufferedWriter(fw)) {
            for (Sentence sentence : data) {
                for (Token token : sentence) {
                    buff.write(String.format("%s %s %s %s%n", token.word, token.label, token.prediction,
                            !token.label.equals(token.prediction) ? "*" : ""));
                }
            }
        } catch (IOException e) {
            Logger.printException(e);
        }
    }

    /**
     * Constructs and trains a perceptron on the given training data. If test data is present, it will be annotated.
     *
     * @param trainData The training data for the perceptron. This can't be null.
     * @param testData  The optional test data.
     * @return The constructed perceptron.
     */
    public static Perceptron pipeline(List<Sentence> trainData, List<Sentence> testData) {
        if (trainData == null) {
            throw new IllegalArgumentException("The training data can't be null.");
        }
        FeatureExtractors.extractAllFeatures(trainData);
        if (testData != null) {
            FeatureExtractors.extractAllFeatures(testData);
        }
        Perceptron p = new Perceptron(numClasses(), FeatureExtractors.stringMapper.numFeatures());
        p.train(trainData, testData, 45);
        return p;
    }

    /**
     * Starting point for the tagger. The first argument is expected to always be a file path to a training file for the
     * tagger.
     *
     * @param args Command line arguments.
     */
    public static void main(String... args) {
        List<Sentence> trainData = null;
        List<Sentence> testData = null;
        if (args.length > 0 && new File(args[0]).exists()) {
            trainData = readData(args[0]);
        } else if (args[0].equals("-r")) {
            trainData = FeatureExtractors.readFromFile(args[1]);
        } else {
            Logger.printString("Use the -help command.\n");
            System.exit(1);
        }
        if (args.length > 2 && new File(args[2]).exists()) {
            testData = readData(args[2]);
        }
        run(trainData, testData, args);
    }

    private static void run(List<Sentence> trainData, List<Sentence> testData, String... args) {
        Perceptron p = pipeline(trainData, testData);
        String train = "-train";
        switch (args.length) {
            case 3:
                if (args[1].equals("-w") && testData == null) {
                    // Save weights
                    p.saveWeights(args[2]);
                    break;
                }
                if (args[1].equals("-p") && testData == null) {
                    // Save predictions
                    savePredictions(trainData, args[2] + train);
                    break;
                }
                if (args[1].equals("-t")) {
                    // Fall through
                }
            case 4:
                if (testData != null) {
                    // Training and test file exist
                    Logger.printString("Confusion matrix of test data:\n");
                    ConfusionMatrix c = new ConfusionMatrix(testData);
                    c.print(Math.min(5, numClasses()));
                    Logger.printString("Accuracy on training data: " + Evaluation.accuracy(trainData) + "\n");
                    Logger.printString("Accuracy on test data: " + Evaluation.accuracy(testData) + "\n");
                }
                break;
            case 5:
                if (args[3].equals("-p") && !new File(args[4] + train).exists() &&
                        !new File(args[4] + "-test").exists() && testData != null) {
                    // Save predictions
                    savePredictions(trainData, args[4] + train);
                    savePredictions(testData, args[4] + "-test");
                }
                break;
            default:
                Logger.printString("Usage: java Tagger <PathToTrainingFile> <Options>\n"); // -r path
                Logger.printString("Options:\n-t <PathToTestFile> [OutputPath]: Path to a test file which will be "
                        + "annotated. If [OutputPath] is not specified, the accuracy of the tagger and a confusion "
                        + "matrix will be printed.");
                Logger.printString("\n-w <OutputPath>: Save the weights of the tagger to the file specified by" +
                        "<OutputPath>.");
                Logger.printString("\n-p <OutputPath>: Save the predictions for the training and test file to the file"
                        + "specified by <OutputPath> plus an appended suffix.");
                Logger.printString("\n-r <InputPath>: Reads sentences and their annotations from a file in the svm-" +
                        "multiclass format and uses this data to train a model.");
                Logger.printString("\n-s <OutputPath>: Saves the sentences enriched with their extracted features in "
                        + "the file given by <OutputPath>.");
        }
    }
}
