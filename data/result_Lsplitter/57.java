/**
 * This method tests the operand field options.
 * @throws Exception if any error occurs during the test.
 */
public void testOperandFieldOptions() throws Exception {
    showTool(tool);
    loadProgram();
    Options options = tool.getOptions(GhidraOptions.CATEGORY_BROWSER_FIELDS);
    List<String> names = getOptionNames(options, "Operands Field");
    verifyOptionNames(names);
    NamespaceWrappedOption namespaceOption = ( (NamespaceWrappedOption)options.getCustomOption(names.get(3),new NamespaceWrappedOption()));
    verifyWordWrappingOption(options, names);
    verifyVariableMarkupOptions(options, names);
    verifyOptions(options, names, namespaceOption);
}
/**
 * Verifies the names of the options.
 * @param names the list of option names.
 */
private void verifyOptionNames(List<String> names) {
    assertEquals(15, names.size());
    assertEquals("Operands Field.Add Space After Separator", names.get(0));
    assertEquals("Operands Field.Always Show Primary Reference", names.get(1));
    assertEquals("Operands Field.Display Abbreviated Default Label Names", names.get(2));
    assertEquals("Operands Field.Display Namespace", names.get(3));
    assertEquals("Operands Field.Enable Word Wrapping", names.get(4));
    assertEquals("Operands Field.Follow Read or Indirect Pointer References", names.get(5));
    assertEquals("Operands Field.Include Scalar Reference Adjustment", names.get(6));
    assertEquals("Operands Field.Markup Inferred Variable References", names.get(7));
    assertEquals("Operands Field.Markup Register Variable References", names.get(8));
    assertEquals("Operands Field.Markup Stack Variable References", names.get(9));
    assertEquals("Operands Field.Maximum Length of String in Default Labels", names.get(10));
    assertEquals("Operands Field.Maximum Lines To Display", names.get(11));
    assertEquals("Operands Field.Show Block Names", names.get(12));
    assertEquals("Operands Field.Show Offcut Information", names.get(13));
    assertEquals("Operands Field.Underline References", names.get(14));
}

/**
 * Verifies the word wrapping option.
 * @param options the options.
 * @param names the list of option names.
 */
private void verifyWordWrappingOption(Options options, List<String> names) throws Exception {
    assertTrue(cb.goToField(addr("0x100eee0"), "Address", 0, 0));
    ListingTextField btf = (ListingTextField) cb.getCurrentField();
    assertEquals(1, getNumberOfLines(btf));
    options.setBoolean(names.get(4), true);
    options.setInt(names.get(11), 4);
    cb.updateNow();
}





/** 
 * Verifies the variable markup options.
 * @param options the options.
 * @param names the list of option names.
 */
private void verifyVariableMarkupOptions(Options options,List<String> names) throws Exception {
  assertTrue(cb.goToField(addr("0x1002d06"),"Operands",0,0));
  ListingTextField btf=(ListingTextField)cb.getCurrentField();
  assertEquals("dword ptr [EBP + param_5]",btf.getText());
  options.setBoolean(names.get(9),false);
  cb.updateNow();
  cb.goToField(addr("0x1002d06"),"Operands",0,0);
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("dword ptr [EBP + 0x14]=>param_5",btf.getText());
  Command cmd=new AddRegisterRefCmd(addr("0x1002d0b"),0,program.getRegister("EDI"),SourceType.USER_DEFINED);
  applyCmd(program,cmd);
  cb.updateNow();
  assertTrue(cb.goToField(addr("0x1002d0b"),"Operands",0,0));
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("local_EDI_22,EAX",btf.getText());
  assertTrue(cb.goToField(addr("0x1002d0f"),"Operands",0,0));
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("local_EDI_22,local_EDI_22",btf.getText());
  options.setBoolean(names.get(7),true);
  cb.updateNow();
  cb.goToField(addr("0x1002d0f"),"Operands",0,0);
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("local_EDI_22,local_EDI_22",btf.getText());
  options.setBoolean(names.get(8),false);
  cb.updateNow();
  cb.goToField(addr("0x1002d0f"),"Operands",0,0);
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("EDI,EDI",btf.getText());
  cb.goToField(addr("0x1002d0b"),"Operands",0,0);
  btf=(ListingTextField)cb.getCurrentField();
  assertEquals("EDI=>local_EDI_22,EAX",btf.getText());
}


private void verifyOptions(Options options, List<String> names, NamespaceWrappedOption namespaceOption) throws Exception {
    cb.goToField(addr("0x100eee0"),"Operands",0,0);
    ListingTextField btf=(ListingTextField)cb.getCurrentField();
    assertEquals(4,getNumberOfLines(btf));
    options.setBoolean(names.get(4),false);
    cb.updateNow();
    cb.goToField(addr("0x100eee0"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals(1,getNumberOfLines(btf));
    assertTrue(cb.goToField(addr("0x10061a7"),"Operands",0,0));
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("dword ptr [DAT_01008044]",btf.getText());
    options.setBoolean(names.get(12),true);
    cb.updateNow();
    cb.goToField(addr("0x10061a7"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("dword ptr [.data:DAT_01008044]",btf.getText());
    cb.goToField(addr("0x1003daa"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("TestLib::ExtNS::ExtLab",btf.getText());
    cb.goToField(addr("0x1001012"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("->TestLib::ExtNS::ExtLab",btf.getText());
    namespaceOption.setShowLibraryInNamespace(false);
    options.setCustomOption(names.get(3),namespaceOption);
    cb.updateNow();
    cb.goToField(addr("0x1003daa"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("ExtNS::ExtLab",btf.getText());
    cb.goToField(addr("0x1001012"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("->ExtNS::ExtLab",btf.getText());
    namespaceOption.setShowLibraryInNamespace(true);
    namespaceOption.setShowNonLocalNamespace(false);
    options.setCustomOption(names.get(3),namespaceOption);
    cb.updateNow();
    cb.goToField(addr("0x1003daa"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("ExtLab",btf.getText());
    cb.goToField(addr("0x1001012"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("->ExtLab",btf.getText());
    options.setBoolean(names.get(5),false);
    cb.updateNow();
    cb.goToField(addr("0x1001012"),"Operands",0,0);
    btf=(ListingTextField)cb.getCurrentField();
    assertEquals("PTR_ExtLab_01003daa",btf.getText());
}
