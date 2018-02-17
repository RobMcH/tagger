package tagger.data;

import tagger.Tagger;
import tagger.utility.BidirectionalMap;
import tagger.utility.Logger;

import java.io.*;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Maps strings to unique identifiers and vice versa.
 */
public class StringMapper implements Serializable {
    private final BidirectionalMap<String, Integer> map = new BidirectionalMap<>();

    /**
     * Returns the unique identifier for the given string.
     *
     * @param s The given string.
     * @return The unique identifier for the given string.
     */
    public int lookup(String s) {
        if (map.keyMapContainsKey(s)) {
            return map.keyMapGet(s);
        } else {
            // If the string is not known yet, store it and return the identifier.
            map.put(s, map.size());
            return map.size() - 1;
        }
    }

    /**
     * Returns the corresponding string to a unique identifier.
     *
     * @param featureIndex The unique identifier.
     * @return The corresponding string.
     */
    public String inverseLookup(int featureIndex) {
        if (map.valueMapContainsKey(featureIndex)) {
            return map.valueMapGet(featureIndex);
        }
        return null;
    }

    /**
     * Serializes the StringMapper.
     *
     * @param filename The name of the file in which the StringMapper will be stored.
     */
    public void toFile(String filename) {
        try (FileOutputStream fos = new FileOutputStream(filename); ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(this);
        } catch (IOException e) {
            Logger.printException(e);
        }
    }

    /**
     * Deserializes the StringMapper.
     *
     * @param filename The name of the file in which the StringMapper is stored.
     * @return The reconstructed StringMapper.
     */
    public static StringMapper fromFile(String filename) {
        try (FileInputStream fis = new FileInputStream(filename); ObjectInputStream ois = new ObjectInputStream(fis)) {
            return (StringMapper) ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            Logger.printException(e);
        }
        return null;
    }

    /**
     * Returns the number of unique features stored in the StringMapper. The number of classes observed in the tagger
     * is added to the feature amount since every token has a feature for the previous predicted class which will be
     * added in the training procedure.
     */
    public int numFeatures() {
        return map.size() + Tagger.numClasses();
    }

    /**
     * Clears the internal maps of the StringMapper.
     */
    public void clear() {
        this.map.clear();
    }
}
