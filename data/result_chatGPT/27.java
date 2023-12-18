/**
 * Asserts the invariants of the map.
 *
 * @param map the map to check
 */
protected final void assertInvariants(Map<K, V> map) {
  Set<K> keySet = map.keySet();
  Collection<V> valueCollection = map.values();
  Set<Entry<K, V>> entrySet = map.entrySet();
  assertMapSizeInvariants(map, keySet, valueCollection, entrySet);
  assertKeySetInvariants(map, keySet);
  assertValueCollectionInvariants(map, valueCollection);
  assertEntrySetInvariants(map, entrySet);
  assertMoreInvariants(map);
}
/**
 * Asserts the size invariants of the map, key set, value collection, and entry set.
 *
 * @param map the map to check
 * @param keySet the key set to check
 * @param valueCollection the value collection to check
 * @param entrySet the entry set to check
 */
private void assertMapSizeInvariants(Map<K, V> map, Set<K> keySet, Collection<V> valueCollection, Set<Entry<K, V>> entrySet) {
  assertEquals(map.size() == 0, map.isEmpty());
  assertEquals(map.size(), keySet.size());
  assertEquals(map.size(), valueCollection.size());
  assertEquals(map.size(), entrySet.size());
}
/**
 * Asserts the invariants of the key set.
 *
 * @param map the map to check
 * @param keySet the key set to check
 */
private void assertKeySetInvariants(Map<K, V> map, Set<K> keySet) {
  assertEquals(keySet.size() == 0, keySet.isEmpty());
  assertEquals(!keySet.isEmpty(), keySet.iterator().hasNext());
  int expectedKeySetHash = 0;
  for (K key : keySet) {
    V value = map.get(key);
    expectedKeySetHash += key != null ? key.hashCode() : 0;
    assertTrue(map.containsKey(key));
    assertTrue(map.containsValue(value));
    assertTrue(allowsNullKeys || (key != null));
  }
  assertEquals(expectedKeySetHash, keySet.hashCode());
}
/**
 * Asserts the invariants of the value collection.
 *
 * @param map the map to check
 * @param valueCollection the value collection to check
 */
private void assertValueCollectionInvariants(Map<K, V> map, Collection<V> valueCollection) {
  assertEquals(valueCollection.size() == 0, valueCollection.isEmpty());
  assertEquals(!valueCollection.isEmpty(), valueCollection.iterator().hasNext());
  for (V value : valueCollection) {
    assertTrue(map.containsValue(value));
    assertTrue(allowsNullValues || (value != null));
  }
}
/**
 * Asserts the invariants of the entry set.
 *
 * @param map the map to check
 * @param entrySet the entry set to check
 */
private void assertEntrySetInvariants(Map<K, V> map, Set<Entry<K, V>> entrySet) {
  assertEquals(entrySet.size() == 0, entrySet.isEmpty());
  assertEquals(!entrySet.isEmpty(), entrySet.iterator().hasNext());
  assertEntrySetNotContainsString(entrySet);
  boolean supportsValuesHashCode = supportsValuesHashCode(map);
  if (supportsValuesHashCode) {
    assertEntrySetHashInvariants(map, entrySet);
  }
  assertEntrySetToArrayInvariants(map, entrySet);
}
/**
 * Asserts the hash invariants of the entry set.
 *
 * @param map the map to check
 * @param entrySet the entry set to check
 */
private void assertEntrySetHashInvariants(Map<K, V> map, Set<Entry<K, V>> entrySet) {
  int expectedEntrySetHash = 0;
  for (Entry<K, V> entry : entrySet) {
    assertTrue(map.containsKey(entry.getKey()));
    assertTrue(map.containsValue(entry.getValue()));
    int expectedHash =
        (entry.getKey() == null ? 0 : entry.getKey().hashCode())
            ^ (entry.getValue() == null ? 0 : entry.getValue().hashCode());
    assertEquals(expectedHash, entry.hashCode());
    expectedEntrySetHash += expectedHash;
  }
  assertEquals(expectedEntrySetHash, entrySet.hashCode());
  assertTrue(entrySet.containsAll(new HashSet<Entry<K, V>>(entrySet)));
  assertTrue(entrySet.equals(new HashSet<Entry<K, V>>(entrySet)));
}
/**
 * Asserts the toArray invariants of the entry set.
 *
 * @param map the map to check
 * @param entrySet the entry set to check
 */
private void assertEntrySetToArrayInvariants(Map<K, V> map, Set<Entry<K, V>> entrySet) {
  Object[] entrySetToArray1 = entrySet.toArray();
  assertEquals(map.size(), entrySetToArray1.length);
  assertTrue(Arrays.asList(entrySetToArray1).containsAll(entrySet));
  Entry<?, ?>[] entrySetToArray2 = new Entry<?, ?>[map.size() + 2];
  entrySetToArray2[map.size()] = mapEntry("foo", 1);
  assertSame(entrySetToArray2, entrySet.toArray(entrySetToArray2));
  assertNull(entrySetToArray2[map.size()]);
  assertTrue(Arrays.asList(entrySetToArray2).containsAll(entrySet));
}
