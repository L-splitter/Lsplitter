public void testAddSynchronizedDomainObject() throws IOException {

	assertNull(obj1.getCurrentTransactionInfo());
	assertNull(obj2.getCurrentTransactionInfo());

	assertEquals(1, obj1.getUndoStackDepth());
	assertEquals(1, obj2.getUndoStackDepth());

	assertTrue(obj1.canUndo());
	assertTrue(obj2.canUndo());
	assertFalse(obj1.canRedo());
	assertFalse(obj2.canRedo());

	TransactionInfo tx = obj1Listener.getLastTransaction();
	obj1Listener.getEvents();
	assertNotNull(tx);

	tx = obj2Listener.getLastTransaction();
	obj2Listener.getEvents();
	assertNotNull(tx);

	assertNull(obj1.getSynchronizedDomainObjects());
	assertNull(obj2.getSynchronizedDomainObjects());

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

	// Test rollback (non-committed) transaction

	int txId1 = obj1.startTransaction("Test1");
	try {

		assertNotNull(obj2.getCurrentTransactionInfo());

		propertyList1.setString("A1.B1", "TestB1");

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
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
			obj2.endTransaction(txId2, true);
		}

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
		assertTrue(Arrays.equals(new String[] {}, events1));
		assertTrue(Arrays.equals(new String[] {}, events2));

		assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
		assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));

	}
	finally {
		obj1.endTransaction(txId1, false);
	}

	// obj2 rollback causes obj2 to rollback
	assertEquals("NULL", propertyList1.getString("A1.B1", "NULL"));
	assertEquals("NULL", propertyList2.getString("A2.B2", "NULL"));

	assertNull(obj1.getCurrentTransactionInfo());

	assertEquals(0, obj1.getUndoStackDepth());
	assertEquals(0, obj2.getUndoStackDepth());

	assertFalse(obj1.canUndo());
	assertFalse(obj2.canUndo());
	assertFalse(obj1.canRedo());
	assertFalse(obj2.canRedo());

	events1 = obj1Listener.getEvents();
	events2 = obj2Listener.getEvents();
	assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE1 }, events1));
	assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE2 }, events2));

	// Test committed transaction

	txId1 = obj1.startTransaction("Test1");
	try {

		assertNotNull(obj2.getCurrentTransactionInfo());

		propertyList1.setString("A1.B1", "TestB1");

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
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
			obj2.endTransaction(txId2, true);
		}

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
		assertTrue(Arrays.equals(new String[] {}, events1));
		assertTrue(Arrays.equals(new String[] {}, events2));

		assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
		assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));

	}
	finally {
		obj1.endTransaction(txId1, true);
	}

	assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
	assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));

	assertNull(obj1.getCurrentTransactionInfo());

	assertEquals(1, obj1.getUndoStackDepth());
	assertEquals(1, obj2.getUndoStackDepth());

	assertTrue(obj1.canUndo());
	assertTrue(obj2.canUndo());
	assertFalse(obj1.canRedo());
	assertFalse(obj2.canRedo());

	events1 = obj1Listener.getEvents();
	events2 = obj2Listener.getEvents();
	assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE1 }, events1));
	assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE2 }, events2));

	assertEquals("obj1: Test1\nobj2: Test2", obj1.getUndoName());
	assertEquals("obj1: Test1\nobj2: Test2", obj2.getUndoName());
	assertEquals("", obj1.getRedoName());
	assertEquals("", obj2.getRedoName());

	obj1.undo();

	assertFalse(obj1.canUndo());
	assertFalse(obj2.canUndo());
	assertTrue(obj1.canRedo());
	assertTrue(obj2.canRedo());

	events1 = obj1Listener.getEvents();
	events2 = obj2Listener.getEvents();
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

	events1 = obj1Listener.getEvents();
	events2 = obj2Listener.getEvents();
	assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE1 }, events1));
	assertTrue(Arrays.equals(new String[] { UNDO_STATE_CHANGE2 }, events2));

	assertEquals("TestB1", propertyList1.getString("A1.B1", "NULL"));
	assertEquals("TestB2", propertyList2.getString("A2.B2", "NULL"));

	// Independent transactions

	txId1 = obj1.startTransaction("Test1");
	try {

		assertNull(obj2.getCurrentTransactionInfo());

		propertyList1.setString("A1.C1", "TestC1");

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
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
			obj2.endTransaction(txId2, true);
		}

		events1 = obj1Listener.getEvents();
		events2 = obj2Listener.getEvents();
		assertTrue(Arrays.equals(new String[] {}, events1));
		assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE2 }, events2));

		assertEquals("TestC1", propertyList1.getString("A1.C1", "NULL"));
		assertEquals("TestC2", propertyList2.getString("A2.C2", "NULL"));

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

	events1 = obj1Listener.getEvents();
	events2 = obj2Listener.getEvents();
	assertTrue(Arrays.equals(new String[] { END, UNDO_STATE_CHANGE1 }, events1));
	assertTrue(Arrays.equals(new String[] {}, events2));

	assertEquals("", obj1.getUndoName());
	assertEquals("obj2: Test2", obj2.getUndoName());
	assertEquals("", obj1.getRedoName());
	assertEquals("", obj2.getRedoName());

}