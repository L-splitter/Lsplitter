public void testSegmentStoreComputedValue() {
    QueuingRemovalListener<Object, Object> listener = queuingRemovalListener();
    LocalCache<Object, Object> map =
        makeLocalCache(createCacheBuilder().concurrencyLevel(1).removalListener(listener));
    Segment<Object, Object> segment = map.segments[0];

    Object key = new Object();
    int hash = map.hash(key);
    AtomicReferenceArray<ReferenceEntry<Object, Object>> table = segment.table;
    int index = hash & (table.length() - 1);

    DummyEntry<Object, Object> entry = DummyEntry.create(key, hash, null);
    LoadingValueReference<Object, Object> valueRef = new LoadingValueReference<>();
    entry.setValueReference(valueRef);

    // absent
    Object value = new Object();
    assertTrue(listener.isEmpty());
    assertEquals(0, segment.count);
    assertNull(segment.get(key, hash));
    assertTrue(segment.storeLoadedValue(key, hash, valueRef, value));
    assertSame(value, segment.get(key, hash));
    assertEquals(1, segment.count);
    assertTrue(listener.isEmpty());

    // clobbered
    Object value2 = new Object();
    assertFalse(segment.storeLoadedValue(key, hash, valueRef, value2));
    assertEquals(1, segment.count);
    assertSame(value, segment.get(key, hash));
    RemovalNotification<Object, Object> notification = listener.remove();
    assertEquals(immutableEntry(key, value2), notification);
    assertEquals(RemovalCause.REPLACED, notification.getCause());
    assertTrue(listener.isEmpty());

    // inactive
    Object value3 = new Object();
    map.clear();
    listener.clear();
    assertEquals(0, segment.count);
    table.set(index, entry);
    assertTrue(segment.storeLoadedValue(key, hash, valueRef, value3));
    assertSame(value3, segment.get(key, hash));
    assertEquals(1, segment.count);
    assertTrue(listener.isEmpty());

    // replaced
    Object value4 = new Object();
    DummyValueReference<Object, Object> value3Ref = DummyValueReference.create(value3);
    valueRef = new LoadingValueReference<>(value3Ref);
    entry.setValueReference(valueRef);
    table.set(index, entry);
    assertSame(value3, segment.get(key, hash));
    assertEquals(1, segment.count);
    assertTrue(segment.storeLoadedValue(key, hash, valueRef, value4));
    assertSame(value4, segment.get(key, hash));
    assertEquals(1, segment.count);
    notification = listener.remove();
    assertEquals(immutableEntry(key, value3), notification);
    assertEquals(RemovalCause.REPLACED, notification.getCause());
    assertTrue(listener.isEmpty());

    // collected
    entry.setValueReference(valueRef);
    table.set(index, entry);
    assertSame(value3, segment.get(key, hash));
    assertEquals(1, segment.count);
    value3Ref.clear();
    assertTrue(segment.storeLoadedValue(key, hash, valueRef, value4));
    assertSame(value4, segment.get(key, hash));
    assertEquals(1, segment.count);
    notification = listener.remove();
    assertEquals(immutableEntry(key, null), notification);
    assertEquals(RemovalCause.COLLECTED, notification.getCause());
    assertTrue(listener.isEmpty());
  }