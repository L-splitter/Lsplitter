/**
 * Asserts that the given multimap is unmodifiable.
 *
 * @param multimap the multimap to test
 * @param sampleKey a sample key to use for testing
 * @param sampleValue a sample value to use for testing
 * @param <K> the type of keys in the multimap
 * @param <V> the type of values in the multimap
 */
public static <K, V> void assertMultimapIsUnmodifiable(
    Multimap<K, V> multimap, K sampleKey, V sampleValue) {
  List<Entry<K, V>> originalEntries =
      Collections.unmodifiableList(Lists.newArrayList(multimap.entries()));
  assertMultimapRemainsUnmodified(multimap, originalEntries);
  Collection<V> sampleValueAsCollection = Collections.singleton(sampleValue);
  testClear(multimap, originalEntries);
  testAsMapEntrySet(multimap, sampleKey, sampleValueAsCollection, originalEntries);
  testValues(multimap, sampleValue, originalEntries);
  testEntries(multimap, sampleKey, sampleValue, originalEntries);
  testKeys(multimap, sampleKey, originalEntries);
  testKeySet(multimap, sampleKey, originalEntries);
  testGet(multimap, sampleValue, originalEntries);
  testPut(multimap, sampleKey, sampleValue, originalEntries);
  testPutAll(multimap, sampleKey, sampleValueAsCollection, originalEntries);
  testRemove(multimap, sampleKey, sampleValue, originalEntries);
  testRemoveAll(multimap, sampleKey, originalEntries);
  testReplaceValues(multimap, sampleKey, sampleValueAsCollection, originalEntries);
  testAsMap(multimap, sampleKey, sampleValue, originalEntries);
}
private static <K, V> void testClear(Multimap<K, V> multimap, List<Entry<K, V>> originalEntries) {
  try {
    multimap.clear();
    fail("clear succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testAsMapEntrySet(Multimap<K, V> multimap, K sampleKey, Collection<V> sampleValueAsCollection, List<Entry<K, V>> originalEntries) {
  assertSetIsUnmodifiable(
      multimap.asMap().entrySet(), Maps.immutableEntry(sampleKey, sampleValueAsCollection));
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testValues(Multimap<K, V> multimap, V sampleValue, List<Entry<K, V>> originalEntries) {
  if (!multimap.isEmpty()) {
    Collection<V> values = multimap.asMap().entrySet().iterator().next().getValue();
    assertCollectionIsUnmodifiable(values, sampleValue);
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testEntries(Multimap<K, V> multimap, K sampleKey, V sampleValue, List<Entry<K, V>> originalEntries) {
  assertCollectionIsUnmodifiable(multimap.entries(), Maps.immutableEntry(sampleKey, sampleValue));
  for (Entry<K, V> entry : multimap.entries()) {
    assertMapEntryIsUnmodifiable(entry);
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testKeys(Multimap<K, V> multimap, K sampleKey, List<Entry<K, V>> originalEntries) {
  assertMultisetIsUnmodifiable(multimap.keys(), sampleKey);
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testKeySet(Multimap<K, V> multimap, K sampleKey, List<Entry<K, V>> originalEntries) {
  assertSetIsUnmodifiable(multimap.keySet(), sampleKey);
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testGet(Multimap<K, V> multimap, V sampleValue, List<Entry<K, V>> originalEntries) {
  if (!multimap.isEmpty()) {
    K key = multimap.keySet().iterator().next();
    assertCollectionIsUnmodifiable(multimap.get(key), sampleValue);
    assertMultimapRemainsUnmodified(multimap, originalEntries);
  }
}
private static <K, V> void testPut(Multimap<K, V> multimap, K sampleKey, V sampleValue, List<Entry<K, V>> originalEntries) {
  try {
    multimap.put(sampleKey, sampleValue);
    fail("put succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testPutAll(Multimap<K, V> multimap, K sampleKey, Collection<V> sampleValueAsCollection, List<Entry<K, V>> originalEntries) {
  try {
    multimap.putAll(sampleKey, sampleValueAsCollection);
    fail("putAll(K, Iterable) succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testRemove(Multimap<K, V> multimap, K sampleKey, V sampleValue, List<Entry<K, V>> originalEntries) {
  try {
    multimap.remove(sampleKey, sampleValue);
    fail("remove succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testRemoveAll(Multimap<K, V> multimap, K sampleKey, List<Entry<K, V>> originalEntries) {
  try {
    multimap.removeAll(sampleKey);
    fail("removeAll succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testReplaceValues(Multimap<K, V> multimap, K sampleKey, Collection<V> sampleValueAsCollection, List<Entry<K, V>> originalEntries) {
  try {
    multimap.replaceValues(sampleKey, sampleValueAsCollection);
    fail("replaceValues succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
private static <K, V> void testAsMap(Multimap<K, V> multimap, K sampleKey, V sampleValue, List<Entry<K, V>> originalEntries) {
  try {
    multimap.asMap().remove(sampleKey);
    fail("asMap().remove() succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);
  if (!multimap.isEmpty()) {
    K presentKey = multimap.keySet().iterator().next();
    try {
      multimap.asMap().get(presentKey).remove(sampleValue);
      fail("asMap().get().remove() succeeded on unmodifiable multimap");
    } catch (UnsupportedOperationException expected) {
    }
    assertMultimapRemainsUnmodified(multimap, originalEntries);
    try {
      multimap.asMap().values().iterator().next().remove(sampleValue);
      fail("asMap().values().iterator().next().remove() succeeded on unmodifiable multimap");
    } catch (UnsupportedOperationException expected) {
    }
    try {
      ((Collection<?>) multimap.asMap().values().toArray()[0]).clear();
      fail("asMap().values().toArray()[0].clear() succeeded on unmodifiable multimap");
    } catch (UnsupportedOperationException expected) {
    }
  }
  assertCollectionIsUnmodifiable(multimap.values(), sampleValue);
  assertMultimapRemainsUnmodified(multimap, originalEntries);
}
