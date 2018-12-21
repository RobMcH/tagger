package tagger.model;

import tagger.data.Sentence;
import tagger.data.StringMapper;
import tagger.data.Token;
import tagger.utility.Logger;

import java.io.*;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * <p>
 * Extracts the features of a token. Features are:
 * - Previous word
 * - Next word
 * - Current word
 * - All suffices up to a length of 5
 * - All prefixes up to a length of 3
 * - Membership of certain dictionaries (determiners, adjectives, proper nouns), which are constructed from the training
 * data
 * - Occurence of numbers/special characters
 * - Capitalization
 * The extracted features can be stored/read in/from a file using the svm-multiclass format.
 * @see <a href="https://www.cs.cornell.edu/people/tj/svm_light/svm_multiclass.html">svm-multiclass format</a>
 */
public class FeatureExtractors {
    public static final StringMapper stringMapper = new StringMapper();
    // Lists for storing determiners, adjectives and proper nouns seen in the training.
    private static final HashSet<String> determiners = new HashSet<>(30);
    private static final HashSet<String> adjectives = new HashSet<>(5000);
    private static final HashSet<String> properNouns = new HashSet<>(200);
    private static final int featureCount = 11;

    /**
     * Adds a determiner to the list of known determiners.
     *
     * @param s The determiner.
     */
    public static void addDeterminer(String s) {
        determiners.add(s);
    }

    /**
     * Adds an adjective to the list of known adjectives.
     *
     * @param s The adjective.
     */
    public static void addAdjective(String s) {
        adjectives.add(s);
    }

    /**
     * Adds a proper noun to the list of known proper nouns.
     *
     * @param s The determiner.
     */
    public static void addProperNoun(String s) {
        properNouns.add(s);
    }

    private FeatureExtractors() {
    }

    /**
     * Constructs the feature array of the correct size for a given token.
     *
     * @param token The given token.
     */
    private static void constructFeatureArray(Token token) {
        // Suffices up to a length of 5, prefixes up to a length of 3, plus 10 features, plus the previous tag
        int size = Math.min(token.word.length(), 5) + Math.min(token.word.length(), 3) + featureCount;
        token.features = new int[size];
    }

    /**
     * Extracts the word from the previous token of the given token. If the token is at the beginning of a sentence
     * the ID of "prev:BOS" is returned.
     *
     * @param token The given token.
     * @return The ID of the previous word.
     */
    private static int extractPrevWord(Token token) {
        if (token.previous != null) {
            return stringMapper.lookup("prev:" + token.previous.word);
        } else {
            return stringMapper.lookup("prev:" + "BOS");
        }
    }

    /**
     * Extracts the word from the next token of the given token. If the token is at the end of a sentence the ID of
     * "next:EOS" is returned.
     *
     * @param token The given token.
     * @return The ID of the next word.
     */
    private static int extractNextWord(Token token) {
        if (token.next != null) {
            return stringMapper.lookup("next:" + token.next.word);
        } else {
            return stringMapper.lookup("next:" + "EOS");
        }
    }

    /**
     * Extracts the word from the given token.
     *
     * @param token The given token.
     * @return The ID of the word of the current word.
     */
    private static int extractCurrentWord(Token token) {
        return stringMapper.lookup(token.word);
    }

    /**
     * Extracts all suffices up to length of 5 of the word of the given token.
     *
     * @param token The given token.
     */
    private static void extractSuffices(Token token) {
        StringBuilder build = new StringBuilder(5);
        // Loops reversely through the word.
        for (int i = token.word.length() - 1, j = 0; i > 0 && i > token.word.length() - 6; i--) {
            build.append(token.word.charAt(i));
            String suf = build.reverse().toString();
            token.features[j++] = stringMapper.lookup("suf=" + suf);
        }
    }

    /**
     * Extracts all prefixes up to a length of 3 of the word of the given token.
     *
     * @param token The given token.
     */
    private static void extractPrefixes(Token token) {
        StringBuilder build = new StringBuilder(3);
        // Loops through the word
        for (int i = 0, j = Math.min(token.word.length(), 5); i < 3 && i < token.word.length(); i++) {
            build.append(token.word.charAt(i));
            String pre = build.toString();
            token.features[j++] = stringMapper.lookup("pre=" + pre);
        }
    }

    /**
     * Extracts if the first character of the word of the given token is capitalized.
     *
     * @param token The given token.
     * @return The ID of the capitalization case.
     */
    private static int extractCapitalization(Token token) {
        return stringMapper.lookup("cap=" + Character.isUpperCase(token.word.charAt(0)));
    }

    /**
     * Extracts if the the word of the given token contains a digit.
     *
     * @param token The given token.
     * @return The ID of the digit case.
     */
    private static int extractContainsDigit(Token token) {
        for (char c : token.word.toCharArray()) {
            if (Character.isDigit(c)) {
                return stringMapper.lookup("digit=true");
            }
        }
        return stringMapper.lookup("digit=false");
    }

    /**
     * Extracts if the the word of the given token is in the list of known determiners.
     *
     * @param token The given token.
     * @return The ID of the determiner case.
     */
    private static int extractIsDeterminer(Token token) {
        return stringMapper.lookup("det=" + determiners.contains(token.word));
    }

    /**
     * Extracts if the the word of the given token is in the list of known adjectives.
     *
     * @param token The given token.
     * @return The ID of the adjective case.
     */
    private static int extractIsAdjective(Token token) {
        return stringMapper.lookup("adj=" + adjectives.contains(token.word));
    }

    /**
     * Extracts if the the word of the given token is in the list of known proper nouns.
     *
     * @param token The given token.
     * @return The ID of the proper noun case.
     */
    private static int extractIsProperNoun(Token token) {
        return stringMapper.lookup("nnp=" + properNouns.contains(token.word));
    }

    /**
     * Extracts if the word of the given token contains a colon.
     *
     * @param token The given token.
     * @return The ID of the colon case.
     */
    private static int extractContainsColon(Token token) {
        return stringMapper.lookup("period=" + token.word.contains(":"));
    }

    /**
     * Extracts if the word of the given token contains a period.
     *
     * @param token The given token.
     * @return The ID of the period case.
     */
    private static int extractContainsPeriod(Token token) {
        return stringMapper.lookup("dot=" + token.word.contains("."));
    }

    /**
     * Extracts the predicted previous tag of the given token. This means that the model has to
     * have already made a prediction. The feature is directly stored in the spare spot in the feature vector.
     *
     * @param token The given token.
     */
    public static void extractPreviousTag(Token token) {
        if (token.previous != null) {
            token.features[token.features.length - 1] = stringMapper.lookup("prevTag=" +
                    token.previous.predictedLabelIndex);
        }
    }

    /**
     * Extracts all features of a given token and stores them in token.features.
     *
     * @param token The given token.
     */
    public static void extractFeatures(Token token) {
        constructFeatureArray(token);
        extractSuffices(token);
        extractPrefixes(token);
        // Add the remaining features after the suffix and prefix features.
        int i = Math.min(token.word.length(), 5) + Math.min(token.word.length(), 3);
        token.features[i++] = extractCurrentWord(token);
        token.features[i++] = extractPrevWord(token);
        token.features[i++] = extractNextWord(token);
        token.features[i++] = extractCapitalization(token);
        token.features[i++] = extractContainsDigit(token);
        token.features[i++] = extractIsDeterminer(token);
        token.features[i++] = extractIsAdjective(token);
        token.features[i++] = extractIsProperNoun(token);
        token.features[i++] = extractContainsColon(token);
        token.features[i] = extractContainsPeriod(token);
        // One spare feature for storing the predicted previous tag. This will be done in the training.
        token.correctLabelIndex = LabelExtractor.extractLabel(token);
    }

    /**
     * Extracts all features for all tokens in a list of sentences.
     *
     * @param sentences The list of sentences.
     */
    public static void extractAllFeatures(List<Sentence> sentences) {
        sentences.forEach(tokens -> tokens.forEach(FeatureExtractors::extractFeatures));
    }

    /**
     * Stores the extracted features of all tokens in the list of sentences in a file according to the svm-multiclass
     * format.
     *
     * @param sentences The list of sentences.
     * @param filename  The name of the file where the data is stored.
     */
    public static void writeToFile(List<Sentence> sentences, String filename) {
        try (FileWriter fw = new FileWriter(filename); BufferedWriter buff = new BufferedWriter(fw)) {
            int s = 0;
            for (Sentence sent : sentences) {
                int t = 0;
                for (Token token : sent) {
                    // All stored features have the value 1. This is only stored to match the file format.
                    for (int i = 0; i < token.features.length - 1; i++) {
                        buff.write(token.features[i] + ":1 ");
                    }
                    // No space after the last feature.
                    buff.write(token.features[token.features.length - 1] + ":1");
                    // If the token was the last in the current sentence append End of Sentence.
                    if (t == sent.size() - 1) {
                        buff.write(" # EOS");
                    }
                    if (s != sentences.size() - 1 || t != sent.size() - 1) {
                        buff.newLine();
                    }
                    t++;
                }
                s++;
            }
            buff.newLine();
        } catch (IOException e) {
            Logger.printString("IOException occurred with filename " + filename);
            Logger.printException(e);
        }
    }

    /**
     * Reads extracted features from a file in the svm-multiclass format. Some information is lost in this process since
     * only the features are stored.
     *
     * @param filename The name of the file where the features are stored.
     * @return The reconstructed list of sentences.
     */
    public static List<Sentence> readFromFile(String filename) {
        List<Sentence> sentences = new LinkedList<>();
        try (FileReader fr = new FileReader(filename); BufferedReader reader = new BufferedReader(fr)) {
            StringMapper sm = new StringMapper();
            Sentence sent = new Sentence();
            boolean eos = false;

            // Read the entire file and process it line by line. Each line corresponds to one token.
            String s;
            while ((s = reader.readLine()) != null) {
                Token t = new Token();
                String[] strings = s.split(" ");
                if (strings[strings.length - 1].equals("EOS")) {
                    // The current token is the last in the current sentence.
                    eos = true;
                    t.next = null;
                    // The last two elements in strings[] are "#" and "EOS" and therefore no features.
                    t.features = new int[strings.length - 2];
                } else {
                    t.features = new int[strings.length];
                }
                // Loop through all stored features for this token and reconstruct them.
                for (int i = 0; i < strings.length; i++) {
                    String currentFeature = strings[i].split(":")[0];
                    if (i == 0) {
                        // The word of a token is stored at the beginning of each line.
                        t.word = sm.inverseLookup(Integer.parseInt(currentFeature));
                        int featureNumber = Integer.parseInt(currentFeature);
                        t.features[0] = featureNumber;
                    } else {
                        // Reconstruct all features except of "#" and "EOS".
                        if (!eos || i < strings.length - 2) {
                            int featureNumber = Integer.parseInt(currentFeature);
                            t.features[i] = featureNumber;
                        }
                    }
                }
                sent.add(t);
                if (eos) {
                    // Start a new sentence if the previous is finished.
                    sentences.add(sent);
                    sent = new Sentence();
                    eos = false;
                }
            }
        } catch (IOException e) {
            Logger.printString("IOException occurred with filename " + filename);
            Logger.printException(e);
        }
        return sentences;
    }

    /**
     * Sets the previous and next features of a given token contained in a given sentence.
     */
    public static void isEOSorBOS(Sentence sentence, Token token) {
        if (!sentence.isEmpty()) {
            // Set the previous feature of the current token and the next feature of the previous token.
            token.previous = sentence.get(sentence.size() - 1);
            sentence.get(sentence.size() - 1).next = token;
        } else {
            // The token is the beginning of a sentence.
            token.previous = null;
        }
    }
}
