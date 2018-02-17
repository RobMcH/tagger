package tagger.model;

import tagger.data.Token;
import tagger.utility.BidirectionalMap;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * <p>
 * Extracts the label index for a given label.
 */
public class LabelExtractor {
    private static final BidirectionalMap<String, Integer> map = new BidirectionalMap<>();

    private LabelExtractor() {
    }

    /**
     * Stores the label of the given token and returns an unique ID.
     *
     * @param t The given token.
     * @return An unique ID for that label.
     */
    public static int extractLabel(Token t) {
        if (!map.keyMapContainsKey(t.label)) {
            map.put(t.label, map.size());
        }
        return map.keyMapGet(t.label);
    }

    /**
     * Returns the corresponding label to an ID.
     *
     * @param id The ID of the class.
     * @return The corresponding label.
     */
    public static String getLabel(int id) {
        if (map.valueMapContainsKey(id)) {
            return map.valueMapGet(id);
        }
        return null;
    }

    /**
     * Clears the LabelExtractor.
     */
    public static void clear() {
        map.clear();
    }
}
