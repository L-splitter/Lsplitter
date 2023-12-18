public static DoSection parse(XContentParser parser) throws IOException {
    String currentFieldName = null;
    XContentParser.Token token;

    DoSection doSection = new DoSection(parser.getTokenLocation());
    ApiCallSection apiCallSection = null;
    NodeSelector nodeSelector = NodeSelector.ANY;
    Map<String, String> headers = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
    List<String> expectedWarnings = new ArrayList<>();
    List<Pattern> expectedWarningsRegex = new ArrayList<>();
    List<String> allowedWarnings = new ArrayList<>();
    List<Pattern> allowedWarningsRegex = new ArrayList<>();

    if (parser.nextToken() != XContentParser.Token.START_OBJECT) {
        throw new IllegalArgumentException(
            "expected ["
                + XContentParser.Token.START_OBJECT
                + "], "
                + "found ["
                + parser.currentToken()
                + "], the do section is not properly indented"
        );
    }

    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
        if (token == XContentParser.Token.FIELD_NAME) {
            currentFieldName = parser.currentName();
        } else if (token.isValue()) {
            if ("catch".equals(currentFieldName)) {
                doSection.setCatch(parser.text());
            } else {
                throw new ParsingException(parser.getTokenLocation(), "unsupported field [" + currentFieldName + "]");
            }
        } else if (token == XContentParser.Token.START_ARRAY) {
            if ("warnings".equals(currentFieldName)) {
                while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
                    expectedWarnings.add(parser.text());
                }
                if (token != XContentParser.Token.END_ARRAY) {
                    throw new ParsingException(parser.getTokenLocation(), "[warnings] must be a string array but saw [" + token + "]");
                }
            } else if ("warnings_regex".equals(currentFieldName)) {
                while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
                    expectedWarningsRegex.add(Pattern.compile(parser.text()));
                }
                if (token != XContentParser.Token.END_ARRAY) {
                    throw new ParsingException(
                        parser.getTokenLocation(),
                        "[warnings_regex] must be a string array but saw [" + token + "]"
                    );
                }
            } else if ("allowed_warnings".equals(currentFieldName)) {
                while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
                    allowedWarnings.add(parser.text());
                }
                if (token != XContentParser.Token.END_ARRAY) {
                    throw new ParsingException(
                        parser.getTokenLocation(),
                        "[allowed_warnings] must be a string array but saw [" + token + "]"
                    );
                }
            } else if ("allowed_warnings_regex".equals(currentFieldName)) {
                while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
                    allowedWarningsRegex.add(Pattern.compile(parser.text()));
                }
                if (token != XContentParser.Token.END_ARRAY) {
                    throw new ParsingException(
                        parser.getTokenLocation(),
                        "[allowed_warnings_regex] must be a string array but saw [" + token + "]"
                    );
                }
            } else {
                throw new ParsingException(parser.getTokenLocation(), "unknown array [" + currentFieldName + "]");
            }
        } else if (token == XContentParser.Token.START_OBJECT) {
            if ("headers".equals(currentFieldName)) {
                String headerName = null;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        headerName = parser.currentName();
                    } else if (token.isValue()) {
                        headers.put(headerName, parser.text());
                    }
                }
            } else if ("node_selector".equals(currentFieldName)) {
                String selectorName = null;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        selectorName = parser.currentName();
                    } else {
                        NodeSelector newSelector = buildNodeSelector(selectorName, parser);
                        nodeSelector = nodeSelector == NodeSelector.ANY
                            ? newSelector
                            : new ComposeNodeSelector(nodeSelector, newSelector);
                    }
                }
            } else if (currentFieldName != null) { // must be part of API call then
                apiCallSection = new ApiCallSection(currentFieldName);
                String paramName = null;
                while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
                    if (token == XContentParser.Token.FIELD_NAME) {
                        paramName = parser.currentName();
                    } else if (token.isValue()) {
                        if ("body".equals(paramName)) {
                            String body = parser.text();
                            XContentParser bodyParser = JsonXContent.jsonXContent.createParser(XContentParserConfiguration.EMPTY, body);
                            // multiple bodies are supported e.g. in case of bulk provided as a whole string
                            while (bodyParser.nextToken() != null) {
                                apiCallSection.addBody(bodyParser.mapOrdered());
                            }
                        } else {
                            apiCallSection.addParam(paramName, parser.text());
                        }
                    } else if (token == XContentParser.Token.START_OBJECT) {
                        if ("body".equals(paramName)) {
                            apiCallSection.addBody(parser.mapOrdered());
                        }
                    }
                }
            }
        }
    }
    try {
        if (apiCallSection == null) {
            throw new IllegalArgumentException("client call section is mandatory within a do section");
        }
        for (String w : expectedWarnings) {
            if (allowedWarnings.contains(w)) {
                throw new IllegalArgumentException("the warning [" + w + "] was both allowed and expected");
            }
        }
        for (Pattern p : expectedWarningsRegex) {
            if (allowedWarningsRegex.contains(p)) {
                throw new IllegalArgumentException("the warning pattern [" + p + "] was both allowed and expected");
            }
        }
        apiCallSection.addHeaders(headers);
        apiCallSection.setNodeSelector(nodeSelector);
        doSection.setApiCallSection(apiCallSection);
        doSection.setExpectedWarningHeaders(unmodifiableList(expectedWarnings));
        doSection.setExpectedWarningHeadersRegex(unmodifiableList(expectedWarningsRegex));
        doSection.setAllowedWarningHeaders(unmodifiableList(allowedWarnings));
        doSection.setAllowedWarningHeadersRegex(unmodifiableList(allowedWarningsRegex));
    } finally {
        parser.nextToken();
    }
    return doSection;
}