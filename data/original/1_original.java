static List<NamedXContentRegistry.Entry> getDefaultNamedXContents() {
    Map<String, ContextParser<Object, ? extends Aggregation>> map = new HashMap<>();
    map.put(CardinalityAggregationBuilder.NAME, (p, c) -> ParsedCardinality.fromXContent(p, (String) c));
    map.put(InternalHDRPercentiles.NAME, (p, c) -> ParsedHDRPercentiles.fromXContent(p, (String) c));
    map.put(InternalHDRPercentileRanks.NAME, (p, c) -> ParsedHDRPercentileRanks.fromXContent(p, (String) c));
    map.put(InternalTDigestPercentiles.NAME, (p, c) -> ParsedTDigestPercentiles.fromXContent(p, (String) c));
    map.put(InternalTDigestPercentileRanks.NAME, (p, c) -> ParsedTDigestPercentileRanks.fromXContent(p, (String) c));
    map.put(PercentilesBucketPipelineAggregationBuilder.NAME, (p, c) -> ParsedPercentilesBucket.fromXContent(p, (String) c));
    map.put(MedianAbsoluteDeviationAggregationBuilder.NAME, (p, c) -> ParsedMedianAbsoluteDeviation.fromXContent(p, (String) c));
    map.put(MinAggregationBuilder.NAME, (p, c) -> ParsedMin.fromXContent(p, (String) c));
    map.put(MaxAggregationBuilder.NAME, (p, c) -> ParsedMax.fromXContent(p, (String) c));
    map.put(SumAggregationBuilder.NAME, (p, c) -> ParsedSum.fromXContent(p, (String) c));
    map.put(AvgAggregationBuilder.NAME, (p, c) -> ParsedAvg.fromXContent(p, (String) c));
    map.put(WeightedAvgAggregationBuilder.NAME, (p, c) -> ParsedWeightedAvg.fromXContent(p, (String) c));
    map.put(ValueCountAggregationBuilder.NAME, (p, c) -> ParsedValueCount.fromXContent(p, (String) c));
    map.put(InternalSimpleValue.NAME, (p, c) -> ParsedSimpleValue.fromXContent(p, (String) c));
    map.put(DerivativePipelineAggregationBuilder.NAME, (p, c) -> ParsedDerivative.fromXContent(p, (String) c));
    map.put(InternalBucketMetricValue.NAME, (p, c) -> ParsedBucketMetricValue.fromXContent(p, (String) c));
    map.put(StatsAggregationBuilder.NAME, (p, c) -> ParsedStats.fromXContent(p, (String) c));
    map.put(StatsBucketPipelineAggregationBuilder.NAME, (p, c) -> ParsedStatsBucket.fromXContent(p, (String) c));
    map.put(ExtendedStatsAggregationBuilder.NAME, (p, c) -> ParsedExtendedStats.fromXContent(p, (String) c));
    map.put(ExtendedStatsBucketPipelineAggregationBuilder.NAME, (p, c) -> ParsedExtendedStatsBucket.fromXContent(p, (String) c));
    map.put(GeoBoundsAggregationBuilder.NAME, (p, c) -> ParsedGeoBounds.fromXContent(p, (String) c));
    map.put(GeoCentroidAggregationBuilder.NAME, (p, c) -> ParsedGeoCentroid.fromXContent(p, (String) c));
    map.put(HistogramAggregationBuilder.NAME, (p, c) -> ParsedHistogram.fromXContent(p, (String) c));
    map.put(DateHistogramAggregationBuilder.NAME, (p, c) -> ParsedDateHistogram.fromXContent(p, (String) c));
    map.put(AutoDateHistogramAggregationBuilder.NAME, (p, c) -> ParsedAutoDateHistogram.fromXContent(p, (String) c));
    map.put(VariableWidthHistogramAggregationBuilder.NAME, (p, c) -> ParsedVariableWidthHistogram.fromXContent(p, (String) c));
    map.put(StringTerms.NAME, (p, c) -> ParsedStringTerms.fromXContent(p, (String) c));
    map.put(LongTerms.NAME, (p, c) -> ParsedLongTerms.fromXContent(p, (String) c));
    map.put(DoubleTerms.NAME, (p, c) -> ParsedDoubleTerms.fromXContent(p, (String) c));
    map.put(LongRareTerms.NAME, (p, c) -> ParsedLongRareTerms.fromXContent(p, (String) c));
    map.put(StringRareTerms.NAME, (p, c) -> ParsedStringRareTerms.fromXContent(p, (String) c));
    map.put(MissingAggregationBuilder.NAME, (p, c) -> ParsedMissing.fromXContent(p, (String) c));
    map.put(NestedAggregationBuilder.NAME, (p, c) -> ParsedNested.fromXContent(p, (String) c));
    map.put(ReverseNestedAggregationBuilder.NAME, (p, c) -> ParsedReverseNested.fromXContent(p, (String) c));
    map.put(GlobalAggregationBuilder.NAME, (p, c) -> ParsedGlobal.fromXContent(p, (String) c));
    map.put(FilterAggregationBuilder.NAME, (p, c) -> ParsedFilter.fromXContent(p, (String) c));
    map.put(InternalSampler.PARSER_NAME, (p, c) -> ParsedSampler.fromXContent(p, (String) c));
    map.put(GeoHashGridAggregationBuilder.NAME, (p, c) -> ParsedGeoHashGrid.fromXContent(p, (String) c));
    map.put(GeoTileGridAggregationBuilder.NAME, (p, c) -> ParsedGeoTileGrid.fromXContent(p, (String) c));
    map.put(RangeAggregationBuilder.NAME, (p, c) -> ParsedRange.fromXContent(p, (String) c));
    map.put(DateRangeAggregationBuilder.NAME, (p, c) -> ParsedDateRange.fromXContent(p, (String) c));
    map.put(GeoDistanceAggregationBuilder.NAME, (p, c) -> ParsedGeoDistance.fromXContent(p, (String) c));
    map.put(FiltersAggregationBuilder.NAME, (p, c) -> ParsedFilters.fromXContent(p, (String) c));
    map.put(AdjacencyMatrixAggregationBuilder.NAME, (p, c) -> ParsedAdjacencyMatrix.fromXContent(p, (String) c));
    map.put(SignificantLongTerms.NAME, (p, c) -> ParsedSignificantLongTerms.fromXContent(p, (String) c));
    map.put(SignificantStringTerms.NAME, (p, c) -> ParsedSignificantStringTerms.fromXContent(p, (String) c));
    map.put(ScriptedMetricAggregationBuilder.NAME, (p, c) -> ParsedScriptedMetric.fromXContent(p, (String) c));
    map.put(IpRangeAggregationBuilder.NAME, (p, c) -> ParsedBinaryRange.fromXContent(p, (String) c));
    map.put(TopHitsAggregationBuilder.NAME, (p, c) -> ParsedTopHits.fromXContent(p, (String) c));
    map.put(CompositeAggregationBuilder.NAME, (p, c) -> ParsedComposite.fromXContent(p, (String) c));
    map.put(StringStatsAggregationBuilder.NAME, (p, c) -> ParsedStringStats.PARSER.parse(p, (String) c));
    map.put(TopMetricsAggregationBuilder.NAME, (p, c) -> ParsedTopMetrics.PARSER.parse(p, (String) c));
    map.put(TimeSeriesAggregationBuilder.NAME, (p, c) -> ParsedTimeSeries.fromXContent(p, (String) (c)));
    List<NamedXContentRegistry.Entry> entries = map.entrySet()
        .stream()
        .map(entry -> new NamedXContentRegistry.Entry(Aggregation.class, new ParseField(entry.getKey()), entry.getValue()))
        .collect(Collectors.toList());
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
    return entries;
}