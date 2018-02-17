package tagger.utility;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * Class which implements a mutable integer used for counting. It starts with a value of 1.
 */
public class MutableInt implements Comparable<MutableInt> {
    private int value = 1;

    /**
     * Increments the value of the mutable integer by 1.
     */
    public void increment() {
        this.value++;
    }

    /**
     * Returns the value of the mutable integer.
     */
    public int getValue() {
        return this.value;
    }

    /**
     * Used for comparing mutable integers to each other. Returns the difference.
     */
    @Override
    public int compareTo(MutableInt o) {
        return this.value - o.value;
    }
}
