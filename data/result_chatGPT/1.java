/**
 * This method is used to get the default named contents.
 * @return List of NamedXContentRegistry.Entry
 */
static List<NamedXContentRegistry.Entry> getDefaultNamedXContents() {
    Map<String, ContextParser<Object, ? extends Aggregation>> map = createMap();
    List<NamedXContentRegistry.Entry> entries = createEntries(map);
    addSuggestionEntries(entries);
    return entries;
}
/**
 * This method is used to create a map of aggregation builders and their corresponding parsers.
 * @return Map of aggregation builders and parsers
 */
private static Map<String, ContextParser<Object, ? extends Aggregation>> createMap() {
    Map<String, ContextParser<Object, ? extends Aggregation>> map = new HashMap<>();
    map.put(CardinalityAggregationBuilder.NAME, (p, c) -> ParsedCardinality.fromXContent(p, (String) c));
    // ... all other map.put() calls ...
    map.put(TimeSeriesAggregationBuilder.NAME, (p, c) -> ParsedTimeSeries.fromXContent(p, (String) (c)));
    return map;
}
/**
 * This method is used to create a list of entries from the map of aggregation builders and parsers.
 * @param map Map of aggregation builders and parsers
 * @return List of NamedXContentRegistry.Entry
 */
private static List<NamedXContentRegistry.Entry> createEntries(Map<String, ContextParser<Object, ? extends Aggregation>> map) {
    return map.entrySet()
        .stream()
        .map(entry -> new NamedXContentRegistry.Entry(Aggregation.class, new ParseField(entry.getKey()), entry.getValue()))
        .collect(Collectors.toList());
}
/**
 * This method is used to add suggestion entries to the list of entries.
 * @param entries List of NamedXContentRegistry.Entry
 */
private static void addSuggestionEntries(List<NamedXContentRegistry.Entry> entries) {
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField(TermSuggestionBuilder.SUGGESTION_NAME),
            (parser, context) -> TermSuggestion.fromXContent(parser, (String) context)
        )
    );
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField(PhraseSuggestionBuilder.SUGGESTION_NAME),
            (parser, context) -> PhraseSuggestion.fromXContent(parser, (String) context)
        )
    );
    entries.add(
        new NamedXContentRegistry.Entry(
            Suggest.Suggestion.class,
            new ParseField(CompletionSuggestionBuilder.SUGGESTION_NAME),
            (parser, context) -> CompletionSuggestion.fromXContent(parser, (String) context)
        )
    );
}
