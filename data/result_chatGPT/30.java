/**
 * This method tests the unmodifiable navigable map.
 */
public void testUnmodifiableNavigableMap() {
  TreeMap<Integer, String> mod = createTreeMap();
  NavigableMap<Integer, String> unmod = unmodifiableNavigableMap(mod);
  mod.put(4, "four");
  testUnmodifiableMapView(unmod);
  testUnmodifiableMapValues(unmod.values());
  testUnmodifiableMapEntries(unmod.entrySet());
}
/**
 * This method creates a tree map with some initial values.
 * @return a TreeMap with initial values.
 */
private TreeMap<Integer, String> createTreeMap() {
  TreeMap<Integer, String> mod = Maps.newTreeMap();
  mod.put(1, "one");
  mod.put(2, "two");
  mod.put(3, "three");
  return mod;
}
/**
 * This method tests the view of the unmodifiable map.
 * @param unmod the unmodifiable map to test.
 */
private void testUnmodifiableMapView(NavigableMap<Integer, String> unmod) {
  assertEquals("four", unmod.get(4));
  assertEquals("four", unmod.descendingMap().get(4));
  ensureNotDirectlyModifiable(unmod);
  ensureNotDirectlyModifiable(unmod.descendingMap());
  ensureNotDirectlyModifiable(unmod.headMap(2, true));
  ensureNotDirectlyModifiable(unmod.subMap(1, true, 3, true));
  ensureNotDirectlyModifiable(unmod.tailMap(2, true));
}
/**
 * This method tests the values of the unmodifiable map.
 * @param values the values of the unmodifiable map to test.
 */
private void testUnmodifiableMapValues(Collection<String> values) {
  testUnsupportedOperationException(values::add, "4");
  testUnsupportedOperationException(values::remove, "four");
  testUnsupportedOperationException(values::removeAll, Collections.singleton("four"));
  testUnsupportedOperationException(values::retainAll, Collections.singleton("four"));
  testUnsupportedOperationException(() -> {
    Iterator<String> iterator = values.iterator();
    iterator.next();
    iterator.remove();
  });
}
/**
 * This method tests the entries of the unmodifiable map.
 * @param entries the entries of the unmodifiable map to test.
 */
private void testUnmodifiableMapEntries(Set<Entry<Integer, String>> entries) {
  testUnsupportedOperationException(() -> {
    Iterator<Entry<Integer, String>> iterator = entries.iterator();
    iterator.next();
    iterator.remove();
  });
  testUnsupportedOperationExceptionOnEntry(entries.iterator().next(), "four");
  testUnsupportedOperationExceptionOnEntry(entries.toArray(new Entry[0])[0], "four");
}
/**
 * This method tests if an UnsupportedOperationException is thrown when a certain action is performed.
 * @param action the action to perform.
 * @param argument the argument to pass to the action.
 */
private <T> void testUnsupportedOperationException(Consumer<T> action, T argument) {
  try {
    action.accept(argument);
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
}
/**
 * This method tests if an UnsupportedOperationException is thrown when a certain action is performed.
 * @param action the action to perform.
 */
private void testUnsupportedOperationException(Runnable action) {
  try {
    action.run();
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
}
/**
 * This method tests if an UnsupportedOperationException is thrown when the value of an entry is set.
 * @param entry the entry to test.
 * @param value the value to set.
 */
private void testUnsupportedOperationExceptionOnEntry(Entry<Integer, String> entry, String value) {
  testUnsupportedOperationException(entry::setValue, value);
}
