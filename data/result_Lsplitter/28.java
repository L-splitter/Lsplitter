/**
 * This method tests the getIfPresent method of the cache.
 */
public void testGetIfPresent() {
    Cache<Object, Object> cache = CacheBuilder.newBuilder().recordStats().build();
    Object one = new Object();
    Object two = new Object();
    testInitialCacheStats(cache);
    testCacheMiss(cache, one, 1);
    testCacheMiss(cache, two, 2);
    testCacheHitAfterPut(cache, one, two, 1);
    testCacheMiss(cache, two, 3);
    testCacheHitAfterPut(cache, two, one, 2);
    testCacheHit(cache, one, two, 3);
}
/**
 * This method tests the initial cache stats.
 * @param cache The cache to be tested.
 */
private void testInitialCacheStats(Cache<Object, Object> cache) {
    CacheStats stats = cache.stats();
    assertEquals(0, stats.missCount());
    assertEquals(0, stats.loadSuccessCount());
    assertEquals(0, stats.loadExceptionCount());
    assertEquals(0, stats.hitCount());
}
/**
 * This method tests the cache miss.
 * @param cache The cache to be tested.
 * @param key The key to be tested.
 * @param expectedMissCount The expected miss count.
 */
private void testCacheMiss(Cache<Object, Object> cache, Object key, int expectedMissCount) {
    assertNull(cache.getIfPresent(key));
    CacheStats stats = cache.stats();
    assertEquals(expectedMissCount, stats.missCount());
    assertEquals(0, stats.loadSuccessCount());
    assertEquals(0, stats.loadExceptionCount());
    assertEquals(0, stats.hitCount());
    assertNull(cache.asMap().get(key));
    assertFalse(cache.asMap().containsKey(key));
}
/**
 * This method tests the cache hit after put.
 * @param cache The cache to be tested.
 * @param key The key to be tested.
 * @param value The value to be tested.
 * @param expectedHitCount The expected hit count.
 */
private void testCacheHitAfterPut(Cache<Object, Object> cache, Object key, Object value, int expectedHitCount) {
    cache.put(key, value);
    assertSame(value, cache.getIfPresent(key));
    CacheStats stats = cache.stats();
    assertEquals(expectedHitCount, stats.hitCount());
    assertSame(value, cache.asMap().get(key));
    assertTrue(cache.asMap().containsKey(key));
    assertTrue(cache.asMap().containsValue(value));
}
/**
 * This method tests the cache hit.
 * @param cache The cache to be tested.
 * @param key The key to be tested.
 * @param value The value to be tested.
 * @param expectedHitCount The expected hit count.
 */
private void testCacheHit(Cache<Object, Object> cache, Object key, Object value, int expectedHitCount) {
    assertSame(value, cache.getIfPresent(key));
    CacheStats stats = cache.stats();
    assertEquals(expectedHitCount, stats.hitCount());
    assertSame(value, cache.asMap().get(key));
    assertTrue(cache.asMap().containsKey(key));
    assertTrue(cache.asMap().containsValue(value));
}
