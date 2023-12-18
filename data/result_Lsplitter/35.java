/**
 * This method calls all the testSmear methods.
 */
public void testSmear() {
  testSmearValues();
  
  assertEquals(-1017931171,smear(1));

}

/** 
 * This method tests the smear function with both positive and negative input values.
 */
public void testSmearValues(){
  assertEquals(1459320713,smear(754102528));
  assertEquals(-160560296,smear(1234567890));
  assertEquals(-809843551,smear(2000000000));
  assertEquals(766424523,smear(1033096058));
  assertEquals(-1350072884,smear(-2000000000));
  assertEquals(-309370926,smear(-1155484576));
  assertEquals(-1645495900,smear(-723955400));
}
