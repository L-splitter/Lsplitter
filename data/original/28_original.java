public void testGetIfPresent() {
  Cache<Object, Object> cache = CacheBuilder.newBuilder().recordStats().build();
  CacheStats stats = cache.stats();
  assertEquals(0, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(0, stats.hitCount());

  Object one = new Object();
  Object two = new Object();

  assertNull(cache.getIfPresent(one));
  stats = cache.stats();
  assertEquals(1, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(0, stats.hitCount());
  assertNull(cache.asMap().get(one));
  assertFalse(cache.asMap().containsKey(one));
  assertFalse(cache.asMap().containsValue(two));

  assertNull(cache.getIfPresent(two));
  stats = cache.stats();
  assertEquals(2, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(0, stats.hitCount());
  assertNull(cache.asMap().get(two));
  assertFalse(cache.asMap().containsKey(two));
  assertFalse(cache.asMap().containsValue(one));

  cache.put(one, two);

  assertSame(two, cache.getIfPresent(one));
  stats = cache.stats();
  assertEquals(2, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(1, stats.hitCount());
  assertSame(two, cache.asMap().get(one));
  assertTrue(cache.asMap().containsKey(one));
  assertTrue(cache.asMap().containsValue(two));

  assertNull(cache.getIfPresent(two));
  stats = cache.stats();
  assertEquals(3, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(1, stats.hitCount());
  assertNull(cache.asMap().get(two));
  assertFalse(cache.asMap().containsKey(two));
  assertFalse(cache.asMap().containsValue(one));

  cache.put(two, one);

  assertSame(two, cache.getIfPresent(one));
  stats = cache.stats();
  assertEquals(3, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(2, stats.hitCount());
  assertSame(two, cache.asMap().get(one));
  assertTrue(cache.asMap().containsKey(one));
  assertTrue(cache.asMap().containsValue(two));

  assertSame(one, cache.getIfPresent(two));
  stats = cache.stats();
  assertEquals(3, stats.missCount());
  assertEquals(0, stats.loadSuccessCount());
  assertEquals(0, stats.loadExceptionCount());
  assertEquals(3, stats.hitCount());
  assertSame(one, cache.asMap().get(two));
  assertTrue(cache.asMap().containsKey(two));
  assertTrue(cache.asMap().containsValue(one));
}