/**
 * This method tests the creation of an UpdateRequest from XContent.
 * It is the main method that calls other helper methods to perform the test.
 */
public void testFromXContent() throws Exception {
    testSimpleScript();
    testSimpleVerboseScript();
    testScriptWithParams();
    testScriptWithParamsUpsert();
    testScriptWithDoc();
}
/**
 * This method tests the creation of a simple script from XContent.
 */
private void testSimpleScript() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    request.fromXContent(createParser(XContentFactory.jsonBuilder().startObject().field("script", "script1").endObject()));
    validateScript(request.script(), "script1", emptyMap());
}
/**
 * This method tests the creation of a simple verbose script from XContent.
 */
private void testSimpleVerboseScript() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder().startObject().startObject("script").field("source", "script1").endObject().endObject()
        )
    );
    validateScript(request.script(), "script1", emptyMap());
}
/**
 * This method tests the creation of a script with parameters from XContent.
 */
private void testScriptWithParams() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder()
                .startObject()
                .startObject("script")
                .field("source", "script1")
                .startObject("params")
                .field("param1", "value1")
                .endObject()
                .endObject()
                .endObject()
        )
    );
    Map<String, Object> params = new HashMap<>();
    params.put("param1", "value1");
    validateScript(request.script(), "script1", params);
}
/**
 * This method tests the creation of a script with parameters and upsert from XContent.
 */
private void testScriptWithParamsUpsert() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder()
                .startObject()
                .startObject("script")
                .startObject("params")
                .field("param1", "value1")
                .endObject()
                .field("source", "script1")
                .endObject()
                .startObject("upsert")
                .field("field1", "value1")
                .startObject("compound")
                .field("field2", "value2")
                .endObject()
                .endObject()
                .endObject()
        )
    );
    Map<String, Object> params = new HashMap<>();
    params.put("param1", "value1");
    validateScript(request.script(), "script1", params);
    validateUpsert(request, "field1", "value1", "field2", "value2");
}
/**
 * This method tests the creation of a script with a document from XContent.
 */
private void testScriptWithDoc() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder()
                .startObject()
                .startObject("doc")
                .field("field1", "value1")
                .startObject("compound")
                .field("field2", "value2")
                .endObject()
                .endObject()
                .endObject()
        )
    );
    validateDoc(request, "field1", "value1", "field2", "value2");
}
/**
 * This method validates the script of an UpdateRequest.
 */
private void validateScript(Script script, String expectedIdOrCode, Map<String, Object> expectedParams) {
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo(expectedIdOrCode));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    assertThat(script.getParams(), equalTo(expectedParams));
}
/**
 * This method validates the upsert of an UpdateRequest.
 */
private void validateUpsert(UpdateRequest request, String field1, String value1, String field2, String value2) {
    Map<String, Object> upsertDoc = XContentHelper.convertToMap(
        request.upsertRequest().source(),
        true,
        request.upsertRequest().getContentType()
    ).v2();
    assertThat(upsertDoc.get(field1).toString(), equalTo(value1));
    assertThat(((Map<String, Object>) upsertDoc.get("compound")).get(field2).toString(), equalTo(value2));
}
/**
 * This method validates the document of an UpdateRequest.
 */
private void validateDoc(UpdateRequest request, String field1, String value1, String field2, String value2) {
    Map<String, Object> doc = request.doc().sourceAsMap();
    assertThat(doc.get(field1).toString(), equalTo(value1));
    assertThat(((Map<String, Object>) doc.get("compound")).get(field2).toString(), equalTo(value2));
}
