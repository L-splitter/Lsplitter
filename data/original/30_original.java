public void testUnmodifiableNavigableMap() {
  TreeMap<Integer, String> mod = Maps.newTreeMap();
  mod.put(1, "one");
  mod.put(2, "two");
  mod.put(3, "three");

  NavigableMap<Integer, String> unmod = unmodifiableNavigableMap(mod);

  /* unmod is a view. */
  mod.put(4, "four");
  assertEquals("four", unmod.get(4));
  assertEquals("four", unmod.descendingMap().get(4));

  ensureNotDirectlyModifiable(unmod);
  ensureNotDirectlyModifiable(unmod.descendingMap());
  ensureNotDirectlyModifiable(unmod.headMap(2, true));
  ensureNotDirectlyModifiable(unmod.subMap(1, true, 3, true));
  ensureNotDirectlyModifiable(unmod.tailMap(2, true));

  Collection<String> values = unmod.values();
  try {
    values.add("4");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  try {
    values.remove("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  try {
    values.removeAll(Collections.singleton("four"));
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  try {
    values.retainAll(Collections.singleton("four"));
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  try {
    Iterator<String> iterator = values.iterator();
    iterator.next();
    iterator.remove();
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }

  Set<Entry<Integer, String>> entries = unmod.entrySet();
  try {
    Iterator<Entry<Integer, String>> iterator = entries.iterator();
    iterator.next();
    iterator.remove();
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  Entry<Integer, String> entry = entries.iterator().next();
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.lowerEntry(1);
  assertNull(entry);
  entry = unmod.floorEntry(2);
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.ceilingEntry(2);
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.lowerEntry(2);
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.higherEntry(2);
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.firstEntry();
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  entry = unmod.lastEntry();
  try {
    entry.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
  @SuppressWarnings("unchecked")
  Entry<Integer, String> entry2 = (Entry<Integer, String>) entries.toArray()[0];
  try {
    entry2.setValue("four");
    fail("UnsupportedOperationException expected");
  } catch (UnsupportedOperationException expected) {
  }
}