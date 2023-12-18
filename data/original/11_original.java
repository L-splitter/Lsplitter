public void testSortMVField() throws Exception {
    assertAcked(
        prepareCreate("test").setMapping(
            XContentFactory.jsonBuilder()
                .startObject()
                .startObject("_doc")
                .startObject("properties")
                .startObject("long_values")
                .field("type", "long")
                .endObject()
                .startObject("int_values")
                .field("type", "integer")
                .endObject()
                .startObject("short_values")
                .field("type", "short")
                .endObject()
                .startObject("byte_values")
                .field("type", "byte")
                .endObject()
                .startObject("float_values")
                .field("type", "float")
                .endObject()
                .startObject("double_values")
                .field("type", "double")
                .endObject()
                .startObject("string_values")
                .field("type", "keyword")
                .endObject()
                .endObject()
                .endObject()
                .endObject()
        )
    );
    ensureGreen();

    client().prepareIndex("test")
        .setId(Integer.toString(1))
        .setSource(
            jsonBuilder().startObject()
                .array("long_values", 1L, 5L, 10L, 8L)
                .array("int_values", 1, 5, 10, 8)
                .array("short_values", 1, 5, 10, 8)
                .array("byte_values", 1, 5, 10, 8)
                .array("float_values", 1f, 5f, 10f, 8f)
                .array("double_values", 1d, 5d, 10d, 8d)
                .array("string_values", "01", "05", "10", "08")
                .endObject()
        )
        .get();
    client().prepareIndex("test")
        .setId(Integer.toString(2))
        .setSource(
            jsonBuilder().startObject()
                .array("long_values", 11L, 15L, 20L, 7L)
                .array("int_values", 11, 15, 20, 7)
                .array("short_values", 11, 15, 20, 7)
                .array("byte_values", 11, 15, 20, 7)
                .array("float_values", 11f, 15f, 20f, 7f)
                .array("double_values", 11d, 15d, 20d, 7d)
                .array("string_values", "11", "15", "20", "07")
                .endObject()
        )
        .get();
    client().prepareIndex("test")
        .setId(Integer.toString(3))
        .setSource(
            jsonBuilder().startObject()
                .array("long_values", 2L, 1L, 3L, -4L)
                .array("int_values", 2, 1, 3, -4)
                .array("short_values", 2, 1, 3, -4)
                .array("byte_values", 2, 1, 3, -4)
                .array("float_values", 2f, 1f, 3f, -4f)
                .array("double_values", 2d, 1d, 3d, -4d)
                .array("string_values", "02", "01", "03", "!4")
                .endObject()
        )
        .get();

    refresh();

    SearchResponse searchResponse = client().prepareSearch()
        .setQuery(matchAllQuery())
        .setSize(10)
        .addSort("long_values", SortOrder.ASC)
        .get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).longValue(), equalTo(-4L));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).longValue(), equalTo(1L));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).longValue(), equalTo(7L));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("long_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).longValue(), equalTo(20L));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).longValue(), equalTo(10L));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).longValue(), equalTo(3L));

    searchResponse = client().prepareSearch()
        .setQuery(matchAllQuery())
        .setSize(10)
        .addSort(SortBuilders.fieldSort("long_values").order(SortOrder.DESC).sortMode(SortMode.SUM))
        .get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).longValue(), equalTo(53L));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).longValue(), equalTo(24L));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).longValue(), equalTo(2L));

    searchResponse = client().prepareSearch()
        .setQuery(matchAllQuery())
        .setSize(10)
        .addSort(SortBuilders.fieldSort("long_values").order(SortOrder.DESC).sortMode(SortMode.AVG))
        .get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).longValue(), equalTo(13L));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).longValue(), equalTo(6L));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).longValue(), equalTo(1L));

    searchResponse = client().prepareSearch()
        .setQuery(matchAllQuery())
        .setSize(10)
        .addSort(SortBuilders.fieldSort("long_values").order(SortOrder.DESC).sortMode(SortMode.MEDIAN))
        .get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).longValue(), equalTo(13L));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).longValue(), equalTo(7L));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).longValue(), equalTo(2L));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("int_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(-4));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(1));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(7));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("int_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(20));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(10));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(3));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("short_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(-4));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(1));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(7));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("short_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(20));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(10));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(3));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("byte_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(-4));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(1));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(7));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("byte_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).intValue(), equalTo(20));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).intValue(), equalTo(10));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).intValue(), equalTo(3));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("float_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).floatValue(), equalTo(-4f));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).floatValue(), equalTo(1f));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).floatValue(), equalTo(7f));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("float_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).floatValue(), equalTo(20f));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).floatValue(), equalTo(10f));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).floatValue(), equalTo(3f));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("double_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).doubleValue(), equalTo(-4d));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).doubleValue(), equalTo(1d));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).doubleValue(), equalTo(7d));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("double_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(((Number) searchResponse.getHits().getAt(0).getSortValues()[0]).doubleValue(), equalTo(20d));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(((Number) searchResponse.getHits().getAt(1).getSortValues()[0]).doubleValue(), equalTo(10d));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(((Number) searchResponse.getHits().getAt(2).getSortValues()[0]).doubleValue(), equalTo(3d));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("string_values", SortOrder.ASC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(3)));
    assertThat(searchResponse.getHits().getAt(0).getSortValues()[0], equalTo("!4"));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(searchResponse.getHits().getAt(1).getSortValues()[0], equalTo("01"));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(2)));
    assertThat(searchResponse.getHits().getAt(2).getSortValues()[0], equalTo("07"));

    searchResponse = client().prepareSearch().setQuery(matchAllQuery()).setSize(10).addSort("string_values", SortOrder.DESC).get();

    assertThat(searchResponse.getHits().getTotalHits().value, equalTo(3L));
    assertThat(searchResponse.getHits().getHits().length, equalTo(3));

    assertThat(searchResponse.getHits().getAt(0).getId(), equalTo(Integer.toString(2)));
    assertThat(searchResponse.getHits().getAt(0).getSortValues()[0], equalTo("20"));

    assertThat(searchResponse.getHits().getAt(1).getId(), equalTo(Integer.toString(1)));
    assertThat(searchResponse.getHits().getAt(1).getSortValues()[0], equalTo("10"));

    assertThat(searchResponse.getHits().getAt(2).getId(), equalTo(Integer.toString(3)));
    assertThat(searchResponse.getHits().getAt(2).getSortValues()[0], equalTo("03"));
}