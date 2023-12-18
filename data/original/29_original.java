public static <K, V> void assertMultimapIsUnmodifiable(
    Multimap<K, V> multimap, K sampleKey, V sampleValue) {
  List<Entry<K, V>> originalEntries =
      Collections.unmodifiableList(Lists.newArrayList(multimap.entries()));

  assertMultimapRemainsUnmodified(multimap, originalEntries);

  Collection<V> sampleValueAsCollection = Collections.singleton(sampleValue);

  // Test #clear()
  try {
    multimap.clear();
    fail("clear succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }

  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test asMap().entrySet()
  assertSetIsUnmodifiable(
      multimap.asMap().entrySet(), Maps.immutableEntry(sampleKey, sampleValueAsCollection));

  // Test #values()

  assertMultimapRemainsUnmodified(multimap, originalEntries);
  if (!multimap.isEmpty()) {
    Collection<V> values = multimap.asMap().entrySet().iterator().next().getValue();

    assertCollectionIsUnmodifiable(values, sampleValue);
  }

  // Test #entries()
  assertCollectionIsUnmodifiable(multimap.entries(), Maps.immutableEntry(sampleKey, sampleValue));
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Iterate over every element in the entry set
  for (Entry<K, V> entry : multimap.entries()) {
    assertMapEntryIsUnmodifiable(entry);
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #keys()
  assertMultisetIsUnmodifiable(multimap.keys(), sampleKey);
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #keySet()
  assertSetIsUnmodifiable(multimap.keySet(), sampleKey);
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #get()
  if (!multimap.isEmpty()) {
    K key = multimap.keySet().iterator().next();
    assertCollectionIsUnmodifiable(multimap.get(key), sampleValue);
    assertMultimapRemainsUnmodified(multimap, originalEntries);
  }

  // Test #put()
  try {
    multimap.put(sampleKey, sampleValue);
    fail("put succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #putAll(K, Collection<V>)
  try {
    multimap.putAll(sampleKey, sampleValueAsCollection);
    fail("putAll(K, Iterable) succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #putAll(Multimap<K, V>)
  Multimap<K, V> multimap2 = ArrayListMultimap.create();
  multimap2.put(sampleKey, sampleValue);
  try {
    multimap.putAll(multimap2);
    fail("putAll(Multimap<K, V>) succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #remove()
  try {
    multimap.remove(sampleKey, sampleValue);
    fail("remove succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #removeAll()
  try {
    multimap.removeAll(sampleKey);
    fail("removeAll succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #replaceValues()
  try {
    multimap.replaceValues(sampleKey, sampleValueAsCollection);
    fail("replaceValues succeeded on unmodifiable multimap");
  } catch (UnsupportedOperationException expected) {
  }
  assertMultimapRemainsUnmodified(multimap, originalEntries);

  // Test #asMap()
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