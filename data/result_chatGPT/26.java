/**
 * Asserts the invariants of the map.
 *
 * @param map the map to check
 */
protected final void assertInvariants(Map<K, V> map) {
  Set<K> keySet = map.keySet();
  Collection<V> valueCollection = map.values();
  Set<Entry<K, V>> entrySet = map.entrySet();
  assertMapAndKeySet(map, keySet);
  assertKeySet(keySet, map, valueCollection, entrySet);
  assertValueCollection(valueCollection, map);
  assertEntrySet(entrySet, map);
  assertToArray(entrySet, valueCollection, map);
  assertMoreInvariants(map);
}
/**
 * Asserts the invariants of the map and its key set.
 *
 * @param map the map to check
 * @param keySet the key set to check
 */
private void assertMapAndKeySet(Map<K, V> map, Set<K> keySet) {
  assertEquals(map.size() == 0, map.isEmpty());
  assertEquals(map.size(), keySet.size());
  assertEquals(keySet.size() == 0, keySet.isEmpty());
  assertEquals(!keySet.isEmpty(), keySet.iterator().hasNext());
}
/**
 * Asserts the invariants of the key set.
 *
 * @param keySet the key set to check
 * @param map the map to check
 * @param valueCollection the value collection to check
 * @param entrySet the entry set to check
 */
private void assertKeySet(Set<K> keySet, Map<K, V> map, Collection<V> valueCollection, Set<Entry<K, V>> entrySet) {
  int expectedKeySetHash = 0;
  for (K key : keySet) {
    V value = map.get(key);
    expectedKeySetHash += key != null ? key.hashCode() : 0;
    assertTrue(map.containsKey(key));
    assertTrue(map.containsValue(value));
    assertTrue(valueCollection.contains(value));
    assertTrue(valueCollection.containsAll(Collections.singleton(value)));
    assertTrue(entrySet.contains(mapEntry(key, value)));
    assertTrue(allowsNullKeys || (key != null));
  }
  assertEquals(expectedKeySetHash, keySet.hashCode());
}
/**
 * Asserts the invariants of the value collection.
 *
 * @param valueCollection the value collection to check
 * @param map the map to check
 */
private void assertValueCollection(Collection<V> valueCollection, Map<K, V> map) {
  assertEquals(map.size(), valueCollection.size());
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
 * @param entrySet the entry set to check
 * @param map the map to check
 */
private void assertEntrySet(Set<Entry<K, V>> entrySet, Map<K, V> map) {
  assertEquals(map.size(), entrySet.size());
  assertEquals(entrySet.size() == 0, entrySet.isEmpty());
  assertEquals(!entrySet.isEmpty(), entrySet.iterator().hasNext());
  assertEntrySetNotContainsString(entrySet);
  boolean supportsValuesHashCode = supportsValuesHashCode(map);
  if (supportsValuesHashCode) {
    assertEntrySetWithHash(entrySet, map);
  }
}
/**
 * Asserts the invariants of the entry set with hash.
 *
 * @param entrySet the entry set to check
 * @param map the map to check
 */
private void assertEntrySetWithHash(Set<Entry<K, V>> entrySet, Map<K, V> map) {
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
 * Asserts the invariants of the toArray method.
 *
 * @param entrySet the entry set to check
 * @param valueCollection the value collection to check
 * @param map the map to check
 */
private void assertToArray(Set<Entry<K, V>> entrySet, Collection<V> valueCollection, Map<K, V> map) {
  assertEntrySetToArray(entrySet, map);
  assertValueCollectionToArray(valueCollection, map);
}
/**
 * Asserts the invariants of the entry set's toArray method.
 *
 * @param entrySet the entry set to check
 * @param map the map to check
 */
private void assertEntrySetToArray(Set<Entry<K, V>> entrySet, Map<K, V> map) {
  Object[] entrySetToArray1 = entrySet.toArray();
  assertEquals(map.size(), entrySetToArray1.length);
  assertTrue(Arrays.asList(entrySetToArray1).containsAll(entrySet));
  Entry<?, ?>[] entrySetToArray2 = new Entry<?, ?>[map.size() + 2];
  entrySetToArray2[map.size()] = mapEntry("foo", 1);
  assertSame(entrySetToArray2, entrySet.toArray(entrySetToArray2));
  assertNull(entrySetToArray2[map.size()]);
  assertTrue(Arrays.asList(entrySetToArray2).containsAll(entrySet));
}
/**
 * Asserts the invariants of the value collection's toArray method.
 *
 * @param valueCollection the value collection to check
 * @param map the map to check
 */
private void assertValueCollectionToArray(Collection<V> valueCollection, Map<K, V> map) {
  Object[] valuesToArray1 = valueCollection.toArray();
  assertEquals(map.size(), valuesToArray1.length);
  assertTrue(Arrays.asList(valuesToArray1).containsAll(valueCollection));
  Object[] valuesToArray2 = new Object[map.size() + 2];
  valuesToArray2[map.size()] = "foo";
  assertSame(valuesToArray2, valueCollection.toArray(valuesToArray2));
  assertNull(valuesToArray2[map.size()]);
  assertTrue(Arrays.asList(valuesToArray2).containsAll(valueCollection));
}
