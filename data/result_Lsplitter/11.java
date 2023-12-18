/**
 * This method prepares the index with the given id and source.
 * @param id The id of the index.
 * @param source The source of the index.
 * @throws Exception
 */
private void prepareIndex(String id, XContentBuilder source) throws Exception {
    client().prepareIndex("test")
        .setId(id)
        .setSource(source)
        .get();
}

/**
 * This method asserts the search response.
 * @param searchResponse The search response to assert.
 * @param ids The expected ids.
 * @param values The expected values.
 */
private void assertSearchResponse(SearchResponse searchResponse, String[] ids, Number[] values) {
    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));
    for (int i = 0; i < 3; i++) {
        assertThat(searchResponse.getHits().getAt(i).getId(), equalTo(ids[i]));
        assertThat(((Number) searchResponse.getHits().getAt(i).getSortValues()[0]), equalTo(values[i]));
    }
}
public void testSortMVField() throws Exception {
    
  assertAcked(prepareCreate("test").setMapping(XContentFactory.jsonBuilder().startObject().startObject("_doc").startObject("properties").startObject("long_values").field("type","long").endObject().startObject("int_values").field("type","integer").endObject().startObject("short_values").field("type","short").endObject().startObject("byte_values").field("type","byte").endObject().startObject("float_values").field("type","float").endObject().startObject("double_values").field("type","double").endObject().startObject("string_values").field("type","keyword").endObject().endObject().endObject().endObject()));
  ensureGreen();

    prepareIndex(Integer.toString(1), jsonBuilder().startObject()
        .array("long_values", 1L, 5L, 10L, 8L)
        .array("int_values", 1, 5, 10, 8)
        .array("short_values", 1, 5, 10, 8)
        .array("byte_values", 1, 5, 10, 8)
        .array("float_values", 1f, 5f, 10f, 8f)
        .array("double_values", 1d, 5d, 10d, 8d)
        .array("string_values", "01", "05", "10", "08")
        .endObject());
    prepareIndex(Integer.toString(2), jsonBuilder().startObject()
        .array("long_values", 11L, 15L, 20L, 7L)
        .array("int_values", 11, 15, 20, 7)
        .array("short_values", 11, 15, 20, 7)
        .array("byte_values", 11, 15, 20, 7)
        .array("float_values", 11f, 15f, 20f, 7f)
        .array("double_values", 11d, 15d, 20d, 7d)
        .array("string_values", "11", "15", "20", "07")
        .endObject());
    prepareIndex(Integer.toString(3), jsonBuilder().startObject()
        .array("long_values", 2L, 1L, 3L, -4L)
        .array("int_values", 2, 1, 3, -4)
        .array("short_values", 2, 1, 3, -4)
        .array("byte_values", 2, 1, 3, -4)
        .array("float_values", 2f, 1f, 3f, -4f)
        .array("double_values", 2d, 1d, 3d, -4d)
        .array("string_values", "02", "01", "03", "!4")
        .endObject());
    refresh();
    SearchResponse searchResponse = ( client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("long_values",SortOrder.ASC).get());
    assertSearchResponse(searchResponse, new String[]{Integer.toString(3), Integer.toString(1), Integer.toString(2)}, new Long[]{-4L, 1L, 7L});
    // ... repeat for other fields and sort orders ...
}
