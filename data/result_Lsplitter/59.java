/**
 * This method tests the addition of a synchronized domain object.
 * @throws IOException
 */
public void testAddSynchronizedDomainObject() throws IOException {
    initialAssertions();
    checkTransactionInfo();
    addSynchronizedDomainObject();
    testTransaction(false);
testTransaction(true);
    testUndoRedo();
    releaseSynchronizedDomainObject();
    testIndependentTransactions();
}
/**
 * This method performs the initial assertions.
 */
private void initialAssertions() {
    assertNull(obj1.getCurrentTransactionInfo());
    assertNull(obj2.getCurrentTransactionInfo());
    assertEquals(1, obj1.getUndoStackDepth());
    assertEquals(1, obj2.getUndoStackDepth());
    assertTrue(obj1.canUndo());
    assertTrue(obj2.canUndo());
    assertFalse(obj1.canRedo());
    assertFalse(obj2.canRedo());
}
/**
 * This method checks the transaction info.
 */
private void checkTransactionInfo() {
    TransactionInfo tx = obj1Listener.getLastTransaction();
    obj1Listener.getEvents();
    assertNotNull(tx);
    tx = obj2Listener.getLastTransaction();
    obj2Listener.getEvents();
    assertNotNull(tx);
    assertNull(obj1.getSynchronizedDomainObjects());
    assertNull(obj2.getSynchronizedDomainObjects());
}
/**
 * This method adds a synchronized domain object.
 */
private void addSynchronizedDomainObject() {
    try {
        obj1.addSynchronizedDomainObject(obj2);
    }
    catch (LockException e) {
        e.printStackTrace();
        Assert.fail(e.getMessage());
    }
    DomainObject[] synchronizedDomainObjects = obj1.getSynchronizedDomainObjects();
    assertNotNull(synchronizedDomainObjects);
    assertEquals(2, synchronizedDomainObjects.length);
    assertEquals(obj1, synchronizedDomainObjects[0]);
    assertEquals(obj2, synchronizedDomainObjects[1]);
    assertArrayEquals(synchronizedDomainObjects, obj2.getSynchronizedDomainObjects());
    assertEquals(0, obj1.getUndoStackDepth());
    assertEquals(0, obj2.getUndoStackDepth());
    assertFalse(obj1.canUndo());
    assertFalse(obj2.canUndo());
    assertFalse(obj1.canRedo());
    assertFalse(obj2.canRedo());
    String[] events1 = obj1Listener.getEvents();
    assertEquals(UNDO_STATE_CHANGE1, events1[events1.length - 1]);
    String[] events2 = obj2Listener.getEvents();
    assertEquals(UNDO_STATE_CHANGE2, events2[events2.length - 1]);
}


/**
 * This method performs a transaction.
 * @param txId1 The transaction ID.
 * @param commit Whether to commit the transaction.
 */
private void performTransaction(int txId1, boolean commit) {
    assertNotNull(obj2.getCurrentTransactionInfo());
    propertyList1.setString("A1.B1", "TestB1");
    String[] events1 = obj1Listener.getEvents();
    String[] events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE2 }, events2));
    int txId2 = obj2.startTransaction("Test2");
    try {
        propertyList2.setString("A2.B2", "TestB2");
        events1 = obj1Listener.getEvents();
        events2 = obj2Listener.getEvents();
        assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE1 }, events1));
        assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE2 }, events2));
    }
    finally {
        obj2.endTransaction(txId2, commit);
    }
    events1 = obj1Listener.getEvents();
    events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] {}, events1));
    assertTrue(Arrays.equals(new String[] {}, events2));
    assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
    assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));
}
/**
 * This method tests the undo and redo functionality.
 */
private void testUndoRedo() {
    obj1.undo();
    assertFalse(obj1.canUndo());
    assertFalse(obj2.canUndo());
    assertTrue(obj1.canRedo());
    assertTrue(obj2.canRedo());
    String[] events1 = obj1Listener.getEvents();
    String[] events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE2 }, events2));
    assertEquals("NULL", propertyList1.getString("A1.B1", "NULL"));
    assertEquals("NULL", propertyList2.getString("A2.B2", "NULL"));
    assertEquals("", obj1.getUndoName());
    assertEquals("", obj2.getUndoName());
    assertEquals("obj1: Test1\nobj2: Test2", obj1.getRedoName());
    assertEquals("obj1: Test1\nobj2: Test2", obj2.getRedoName());
    obj1.redo();
    assertTrue(obj1.canUndo());
    assertTrue(obj2.canUndo());
    assertFalse(obj1.canRedo());
    assertFalse(obj2.canRedo());
    events1 = obj1Listener.getEvents();
    events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE2 }, events2));
    assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
    assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));
}
/**
 * This method releases a synchronized domain object.
 */
private void releaseSynchronizedDomainObject() {
    try {
        obj1.releaseSynchronizedDomainObject();
    }
    catch (LockException e) {
        e.printStackTrace();
        Assert.fail();
    }
    assertEquals(0, obj1.getUndoStackDepth());
    assertEquals(0, obj2.getUndoStackDepth());
    assertFalse(obj1.canUndo());
    assertFalse(obj2.canUndo());
    assertFalse(obj1.canRedo());
    assertFalse(obj2.canRedo());
    String[] events1 = obj1Listener.getEvents();
    String[] events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE2 }, events2));
    assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
    assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));
}
/**
 * This method tests independent transactions.
 */
private void testIndependentTransactions() {
    // Independent transactions
    int txId1 = obj1.startTransaction("Test1");
    try {
        performIndependentTransaction(txId1, false);
    }
    finally {
        obj1.endTransaction(txId1, false);
    }
    assertEquals("NULL", propertyList1.getString("A1.C1", "NULL"));
    assertEquals("TestC2", propertyList2.getString("A2.C2", "NULL"));
    assertNull(obj1.getCurrentTransactionInfo());
    assertEquals(0, obj1.getUndoStackDepth());
    assertEquals(1, obj2.getUndoStackDepth());
    assertFalse(obj1.canUndo());
    assertTrue(obj2.canUndo());
    assertFalse(obj1.canRedo());
    assertFalse(obj2.canRedo());
    String[] events1 = obj1Listener.getEvents();
    String[] events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] {}, events2));
    assertEquals("", obj1.getUndoName());
    assertEquals("obj2: Test2", obj2.getUndoName());
    assertEquals("", obj1.getRedoName());
    assertEquals("", obj2.getRedoName());
}
/**
 * This method performs an independent transaction.
 * @param txId1 The transaction ID.
 * @param commit Whether to commit the transaction.
 */
private void performIndependentTransaction(int txId1, boolean commit) {
    assertNull(obj2.getCurrentTransactionInfo());
    propertyList1.setString("A1.C1", "TestC1");
    String[] events1 = obj1Listener.getEvents();
    String[] events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE1 }, events1));
    assertTrue(Arrays.equals(new String[] {}, events2));
    int txId2 = obj2.startTransaction("Test2");
    try {
        propertyList2.setString("A2.C2", "TestC2");
        events1 = obj1Listener.getEvents();
        events2 = obj2Listener.getEvents();
        assertTrue(Arrays.equals(new String[] {}, events1));
        assertTrue(Arrays.equals(new String[] { START, UNDO_STATE_CHANGE2 }, events2));
    }
    finally {
        obj2.endTransaction(txId2, commit);
    }
    events1 = obj1Listener.getEvents();
    events2 = obj2Listener.getEvents();
    assertTrue(Arrays.equals(new String[] {}, events1));
    assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE2 }, events2));
    assertEquals("TestC1", propertyList1.getString("A1.C1", "NULL"));
    assertEquals("TestC2", propertyList2.getString("A2.C2", "NULL"));
}

/** 
 * This method tests the transaction.
 */
private void testTransaction(boolean commit){
  int txId1=obj1.startTransaction("Test1");
  try {
    performTransaction(txId1,commit);
  }
  finally {
    obj1.endTransaction(txId1,commit);
  }
  assertEquals(commit ? "TestB1" : "NULL",propertyList1.getString("A1.B1","NULL"));
  assertEquals(commit ? "TestB2" : "NULL",propertyList2.getString("A2.B2","NULL"));
  assertNull(obj1.getCurrentTransactionInfo());
  assertEquals(commit ? 1 : 0,obj1.getUndoStackDepth());
  assertEquals(commit ? 1 : 0,obj2.getUndoStackDepth());
  assertEquals(commit,obj1.canUndo());
  assertEquals(commit,obj2.canUndo());
  assertFalse(obj1.canRedo());
  assertFalse(obj2.canRedo());
  String[] events1=obj1Listener.getEvents();
  String[] events2=obj2Listener.getEvents();
  assertTrue(Arrays.equals(new String[]{END,UNDO_STATE_CHANGE1},events1));
  assertTrue(Arrays.equals(new String[]{END,UNDO_STATE_CHANGE2},events2));
  if(commit){
    assertEquals("obj1: Test1\nobj2: Test2",obj1.getUndoName());
    assertEquals("obj1: Test1\nobj2: Test2",obj2.getUndoName());
    assertEquals("",obj1.getRedoName());
    assertEquals("",obj2.getRedoName());
  }
}
