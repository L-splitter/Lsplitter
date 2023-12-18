/**
 * This method tests the segment store computed value.
 */
public void testSegmentStoreComputedValue() {
    QueuingRemovalListener<Object, Object> listener = queuingRemovalListener();
    LocalCache<Object, Object> map = ( makeLocalCache(createCacheBuilder().concurrencyLevel(1).removalListener(listener)));
    Segment<Object, Object> segment = map.segments[0];
    Object key = new Object();
    int hash = map.hash(key);
    AtomicReferenceArray<ReferenceEntry<Object, Object>> table = segment.table;
    int index = hash & (table.length() - 1);
    DummyEntry<Object, Object> entry = DummyEntry.create(key, hash, null);
    LoadingValueReference<Object, Object> valueRef = new LoadingValueReference<>();
    entry.setValueReference(valueRef);
    testValue(listener, segment, key, hash, valueRef, true);
testValue(listener, segment, key, hash, valueRef, false);
    testInactiveValue(listener, map, segment, key, hash, valueRef, entry, index);
    testValue(listener, segment, key, hash, valueRef, entry, index, true);
testValue(listener, segment, key, hash, valueRef, entry, index, false);
}


/**
 * This method tests the inactive value.
 */
private void testInactiveValue(QueuingRemovalListener<Object, Object> listener, LocalCache<Object, Object> map, Segment<Object, Object> segment, Object key, int hash, LoadingValueReference<Object, Object> valueRef, DummyEntry<Object, Object> entry, int index) {
    Object value3 = new Object();
    map.clear();
    listener.clear();
    assertEquals(0, segment.count);
    table.set(index, entry);
    assertTrue(segment.storeLoadedValue(key, hash, valueRef, value3));
    assertSame(value3, segment.get(key, hash));
    assertEquals(1, segment.count);
    assertTrue(listener.isEmpty());
}




private void testValue(QueuingRemovalListener<Object,Object> listener, Segment<Object,Object> segment, Object key, int hash, LoadingValueReference<Object,Object> valueRef, DummyEntry<Object,Object> entry, int index, boolean isReplaced){
    Object value4 = new Object();
    if(isReplaced){
        DummyValueReference<Object,Object> value3Ref = DummyValueReference.create(value3);
        valueRef = new LoadingValueReference<>(value3Ref);
    }
    entry.setValueReference(valueRef);
    table.set(index,entry);
    assertSame(value3,segment.get(key,hash));
    assertEquals(1,segment.count);
    if(!isReplaced){
        value3Ref.clear();
    }
    assertTrue(segment.storeLoadedValue(key,hash,valueRef,value4));
    assertSame(value4,segment.get(key,hash));
    assertEquals(1,segment.count);
    RemovalNotification<Object,Object> notification = listener.remove();
    if(isReplaced){
        assertEquals(immutableEntry(key,value3),notification);
        assertEquals(RemovalCause.REPLACED,notification.getCause());
    } else {
        assertEquals(immutableEntry(key,null),notification);
        assertEquals(RemovalCause.COLLECTED,notification.getCause());
    }
    assertTrue(listener.isEmpty());
}


private void testValue(QueuingRemovalListener<Object,Object> listener, Segment<Object,Object> segment, Object key, int hash, LoadingValueReference<Object,Object> valueRef, boolean isAbsent){
    Object value = new Object();
    if(isAbsent){
        assertTrue(listener.isEmpty());
        assertEquals(0,segment.count);
        assertNull(segment.get(key,hash));
        assertTrue(segment.storeLoadedValue(key,hash,valueRef,value));
        assertSame(value,segment.get(key,hash));
        assertEquals(1,segment.count);
        assertTrue(listener.isEmpty());
    } else {
        Object value2 = new Object();
        assertFalse(segment.storeLoadedValue(key,hash,valueRef,value2));
        assertEquals(1,segment.count);
        assertSame(value,segment.get(key,hash));
        RemovalNotification<Object,Object> notification = listener.remove();
        assertEquals(immutableEntry(key,value2),notification);
        assertEquals(RemovalCause.REPLACED,notification.getCause());
        assertTrue(listener.isEmpty());
    }
}
