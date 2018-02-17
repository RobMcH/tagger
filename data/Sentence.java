package tagger.data;

import java.util.ArrayList;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * A wrapper around java.util.ArrayList to store tokens (a sentence).
 */
public class Sentence extends ArrayList<Token> {
    public void addToken(Token token) {
        this.add(token);
    }
}
