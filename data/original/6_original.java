public void testFromXContent() throws Exception {
    UpdateRequest request = new UpdateRequest("test", "1");
    // simple script
    request.fromXContent(createParser(XContentFactory.jsonBuilder().startObject().field("script", "script1").endObject()));
    Script script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    Map<String, Object> params = script.getParams();
    assertThat(params, equalTo(emptyMap()));

    // simple verbose script
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder().startObject().startObject("script").field("source", "script1").endObject().endObject()
        )
    );
    script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    params = script.getParams();
    assertThat(params, equalTo(emptyMap()));

    // script with params
    request = new UpdateRequest("test", "1");
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
    script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    params = script.getParams();
    assertThat(params, notNullValue());
    assertThat(params.size(), equalTo(1));
    assertThat(params.get("param1").toString(), equalTo("value1"));

    request = new UpdateRequest("test", "1");
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
                .endObject()
        )
    );
    script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    params = script.getParams();
    assertThat(params, notNullValue());
    assertThat(params.size(), equalTo(1));
    assertThat(params.get("param1").toString(), equalTo("value1"));

    // script with params and upsert
    request = new UpdateRequest("test", "1");
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
    script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    params = script.getParams();
    assertThat(params, notNullValue());
    assertThat(params.size(), equalTo(1));
    assertThat(params.get("param1").toString(), equalTo("value1"));
    Map<String, Object> upsertDoc = XContentHelper.convertToMap(
        request.upsertRequest().source(),
        true,
        request.upsertRequest().getContentType()
    ).v2();
    assertThat(upsertDoc.get("field1").toString(), equalTo("value1"));
    assertThat(((Map<String, Object>) upsertDoc.get("compound")).get("field2").toString(), equalTo("value2"));

    request = new UpdateRequest("test", "1");
    request.fromXContent(
        createParser(
            XContentFactory.jsonBuilder()
                .startObject()
                .startObject("upsert")
                .field("field1", "value1")
                .startObject("compound")
                .field("field2", "value2")
                .endObject()
                .endObject()
                .startObject("script")
                .startObject("params")
                .field("param1", "value1")
                .endObject()
                .field("source", "script1")
                .endObject()
                .endObject()
        )
    );
    script = request.script();
    assertThat(script, notNullValue());
    assertThat(script.getIdOrCode(), equalTo("script1"));
    assertThat(script.getType(), equalTo(ScriptType.INLINE));
    assertThat(script.getLang(), equalTo(Script.DEFAULT_SCRIPT_LANG));
    params = script.getParams();
    assertThat(params, notNullValue());
    assertThat(params.size(), equalTo(1));
    assertThat(params.get("param1").toString(), equalTo("value1"));
    upsertDoc = XContentHelper.convertToMap(request.upsertRequest().source(), true, request.upsertRequest().getContentType()).v2();
    assertThat(upsertDoc.get("field1").toString(), equalTo("value1"));
    assertThat(((Map<String, Object>) upsertDoc.get("compound")).get("field2").toString(), equalTo("value2"));

    // script with doc
    request = new UpdateRequest("test", "1");
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
    Map<String, Object> doc = request.doc().sourceAsMap();
    assertThat(doc.get("field1").toString(), equalTo("value1"));
    assertThat(((Map<String, Object>) doc.get("compound")).get("field2").toString(), equalTo("value2"));
}