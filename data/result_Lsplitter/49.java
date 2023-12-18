/**
 * This method tests the space boundaries.
 * @throws CodeUnitInsertionException if there is an error inserting a code unit.
 * @throws IOException if there is an error reading or writing data.
 */
public void testAtSpaceBoundaries() throws CodeUnitInsertionException, IOException {
    testUndefinedAddressesAndNullManager();
    testTraceInstructionAndData();
    testUndefinedData();
    testCodeUnits();
    testSingleSpaceBoundaries();
}




/**
 * This method tests undefined data.
 * @throws CodeUnitInsertionException if there is an error inserting a code unit.
 */
private void testUndefinedData() throws CodeUnitInsertionException {
    TraceInstruction iCodeMax;
    try (Transaction tx = b.startTransaction()) {
        iCodeMax = b.addInstruction(0, b.addr(-0x0002), b.host, b.buf(0xf4, 0));
    }
    TraceData uCodePre = manager.undefinedData().getAt(0, b.addr(-0x0003));
    assertUndefinedWithAddr(b.addr(-0x0003), uCodePre);
    TraceData uDataPost = manager.undefinedData().getAt(0, b.data(0x0004));
    assertUndefinedWithAddr(b.data(0x0004), uDataPost);
}
/**
 * This method tests code units.
 */
private void testCodeUnits() {
    TraceInstruction iCodeMax = null;
    TraceData uCodePre = manager.undefinedData().getAt(0, b.addr(-0x0003));
    TraceData dDataMin = null;
    TraceData uDataPost = manager.undefinedData().getAt(0, b.data(0x0004));
    assertEquals(List.of(uCodePre, iCodeMax, dDataMin, uDataPost),
            list(manager.codeUnits().get(0, b.addr(-0x0003), b.data(0x0004), true)));
    assertEquals(List.of(uDataPost, dDataMin, iCodeMax, uCodePre),
            list(manager.codeUnits().get(0, b.addr(-0x0003), b.data(0x0004), false)));
}
/**
 * This method tests single space at those boundaries.
 */
private void testSingleSpaceBoundaries() {
    // Also test single space at those boundaries (should get nothing)
    DBTraceCodeSpace dataSpace = manager.getForSpace(b.language.getDefaultDataSpace(), false);
    assertNotNull(dataSpace);
    assertNull(dataSpace.codeUnits().getBefore(0, b.data(0x0000)));
    assertNull(dataSpace.data().getBefore(0, b.data(0x0000)));
    assertNull(dataSpace.definedUnits().getBefore(0, b.data(0x0000)));
    assertNull(dataSpace.instructions().getBefore(0, b.data(0x0000)));
    assertNull(dataSpace.definedData().getBefore(0, b.data(0x0000)));
    assertNull(dataSpace.undefinedData().getBefore(0, b.data(0x0000)));
    DBTraceCodeSpace codeSpace = manager.getForSpace(b.language.getDefaultSpace(), false);
    assertNotNull(codeSpace);
    assertNull(codeSpace.codeUnits().getAfter(0, b.addr(-0x0001)));
    assertNull(codeSpace.data().getAfter(0, b.addr(-0x0001)));
    assertNull(codeSpace.definedUnits().getAfter(0, b.addr(-0x0001)));
    assertNull(codeSpace.instructions().getAfter(0, b.addr(-0x0001)));
    assertNull(codeSpace.definedData().getAfter(0, b.addr(-0x0001)));
    assertNull(codeSpace.undefinedData().getAfter(0, b.addr(-0x0001)));
}

/** 
 * This method tests trace instruction and data.
 * @throws CodeUnitInsertionException if there is an error inserting a code unit.
 */
private void testTraceInstructionAndData() throws CodeUnitInsertionException {
  TraceInstruction iCodeMax;
  TraceData dDataMin;
  try (Transaction tx=b.startTransaction()){
    iCodeMax=b.addInstruction(0,b.addr(-0x0002),b.host,b.buf(0xf4,0));
    dDataMin=b.addData(0,b.data(0x0000),IntegerDataType.dataType,b.buf(1,2,3,4));
  }
  assertEquals(iCodeMax,manager.codeUnits().getBefore(0,b.data(0x0000)));
  assertEquals(iCodeMax,manager.definedUnits().getFloor(0,b.data(0x4000)));
  assertEquals(iCodeMax,manager.definedUnits().getBefore(0,b.data(0x0000)));
  assertEquals(iCodeMax,manager.instructions().getFloor(0,b.data(0x4000)));
  assertEquals(iCodeMax,manager.instructions().getBefore(0,b.data(0x0000)));
  assertNull(manager.definedData().getFloor(0,b.data(0x4000)));
  assertUndefinedWithAddr(b.addr(-0x0003),manager.undefinedData().getBefore(0,b.data(0x0000)));
  assertUndefinedWithAddr(b.data(0x0000),manager.undefinedData().getCeiling(0,b.addr(-0x0002)));
  assertEquals(dDataMin,manager.codeUnits().getAfter(0,b.addr(-0x0001)));
  assertEquals(dDataMin,manager.definedUnits().getCeiling(0,b.addr(0x4000)));
  assertEquals(dDataMin,manager.definedUnits().getAfter(0,b.addr(-0x0001)));
  assertNull(manager.instructions().getCeiling(0,b.addr(0x4000)));
  assertEquals(dDataMin,manager.definedData().getCeiling(0,b.addr(0x4000)));
  assertEquals(dDataMin,manager.definedData().getAfter(0,b.addr(-0x0001)));
  assertUndefinedWithAddr(b.data(0x0004),manager.undefinedData().getAfter(0,b.addr(-0x0001)));
  assertUndefinedWithAddr(b.addr(-0x0001),manager.undefinedData().getFloor(0,b.data(0x0003)));
  b.trace.undo();
}


/** 
 * This method tests undefined addresses and null manager.
 */
private void testUndefinedAddressesAndNullManager(){
  assertUndefinedWithAddr(b.addr(-0x0001),manager.codeUnits().getBefore(0,b.data(0x0000)));
  assertUndefinedWithAddr(b.data(0x0000),manager.codeUnits().getAfter(0,b.addr(-0x0001)));
  assertUndefinedWithAddr(b.addr(-0x0001),manager.data().getBefore(0,b.data(0x0000)));
  assertUndefinedWithAddr(b.data(0x0000),manager.data().getAfter(0,b.addr(-0x0001)));
  assertUndefinedWithAddr(b.addr(-0x0001),manager.undefinedData().getBefore(0,b.data(0x0000)));
  assertUndefinedWithAddr(b.data(0x0000),manager.undefinedData().getAfter(0,b.addr(-0x0001)));
  assertNull(manager.definedUnits().getBefore(0,b.data(0x0000)));
  assertNull(manager.definedUnits().getAfter(0,b.addr(-0x0001)));
  assertNull(manager.instructions().getBefore(0,b.data(0x0000)));
  assertNull(manager.instructions().getAfter(0,b.addr(-0x0001)));
  assertNull(manager.definedData().getBefore(0,b.data(0x0000)));
  assertNull(manager.definedData().getAfter(0,b.addr(-0x0001)));
}
