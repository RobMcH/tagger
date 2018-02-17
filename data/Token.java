package tagger.data;

import java.io.Serializable;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Used for storing the information of a text token.
 */
public class Token implements Serializable {

    // Gold information.
    public String word;
    public String label;
    public String prediction;

    // Generated information.
    public int correctLabelIndex;
    public int predictedLabelIndex;
    public int[] features;
    public Token previous;
    public Token next;
}
