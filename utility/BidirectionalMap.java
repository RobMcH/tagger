package tagger.utility;

import java.io.Serializable;
import java.util.HashMap;

/**
 * @author Robert McHardy
 * @author Alexander Ehmann
 * A bidirectional HashMap which allows to map K to V and V to K. This assumes each Key and Value pair is unique.
 */
public class BidirectionalMap<K, V> implements Serializable {
    private final HashMap<K, V> keyMap;
    private final HashMap<V, K> valueMap;

    public BidirectionalMap() {
        keyMap = new HashMap<>();
        valueMap = new HashMap<>();
    }

    public BidirectionalMap(int capacity) {
        keyMap = new HashMap<>((int) (capacity / 0.75) + 1);
        valueMap = new HashMap<>((int) (capacity / 0.75) + 1);
    }

    /**
     * Returns true if the keyMap (forward map) contains the given key.
     */
    public boolean keyMapContainsKey(K k) {
        return keyMap.containsKey(k);
    }

    /**
     * Returns true if the valueMap (reverse map) contains the given key.
     */
    public boolean valueMapContainsKey(V v) {
        return valueMap.containsKey(v);
    }

    /**
     * Returns the corresponding value to a key in the keyMap (forward map).
     */
    public V keyMapGet(K k) {
        return keyMap.get(k);
    }

    /**
     * Returns the corresponding value to a key in the valueMap (reverse map).
     */
    public K valueMapGet(V v) {
        return valueMap.get(v);
    }

    /**
     * Adds the pair <k, v> to the keyMap (forward map) and the pair <v, k> to the valueMap (reverse map).
     */
    public void put(K k, V v) {
        keyMap.put(k, v);
        valueMap.put(v, k);
    }

    /**
     * Returns the size of the bidirectional map.
     */
    public int size() {
        return keyMap.size();
    }

    /**
     * Clears the key and value maps.
     */
    public void clear() {
        keyMap.clear();
        valueMap.clear();
    }
}
