/**
 * This method processes the start array token.
 * @param parser The parser to process.
 * @param currentFieldName The current field name.
 * @param expectedWarnings The list of expected warnings.
 * @param expectedWarningsRegex The list of expected warnings regex.
 * @param allowedWarnings The list of allowed warnings.
 * @param allowedWarningsRegex The list of allowed warnings regex.
 * @throws IOException If an I/O error occurs.
 */
private static void processStartArrayToken(XContentParser parser, String currentFieldName, List<String> expectedWarnings, List<Pattern> expectedWarningsRegex, List<String> allowedWarnings, List<Pattern> allowedWarningsRegex) throws IOException {
    XContentParser.Token token;
    if ("warnings".equals(currentFieldName)) {
        while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
            expectedWarnings.add(parser.text());
        }
        checkEndArrayToken(parser, token, "[warnings]");
    } else if ("warnings_regex".equals(currentFieldName)) {
        while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
            expectedWarningsRegex.add(Pattern.compile(parser.text()));
        }
        checkEndArrayToken(parser, token, "[warnings_regex]");
    } else if ("allowed_warnings".equals(currentFieldName)) {
        while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
            allowedWarnings.add(parser.text());
        }
        checkEndArrayToken(parser, token, "[allowed_warnings]");
    } else if ("allowed_warnings_regex".equals(currentFieldName)) {
        while ((token = parser.nextToken()) == XContentParser.Token.VALUE_STRING) {
            allowedWarningsRegex.add(Pattern.compile(parser.text()));
        }
        checkEndArrayToken(parser, token, "[allowed_warnings_regex]");
    } else {
        throw new ParsingException(parser.getTokenLocation(), "unknown array [" + currentFieldName + "]");
    }
}
/**
 * This method checks if the token is an end array token.
 * @param parser The parser to check.
 * @param token The token to check.
 * @param fieldName The field name.
 * @throws IOException If an I/O error occurs.
 */
private static void checkEndArrayToken(XContentParser parser, XContentParser.Token token, String fieldName) throws IOException {
    if (token != XContentParser.Token.END_ARRAY) {
        throw new ParsingException(parser.getTokenLocation(), fieldName + " must be a string array but saw [" + token + "]");
    }
}
/**
 * This method processes the start object token.
 * @param parser The parser to process.
 * @param currentFieldName The current field name.
 * @param headers The headers map.
 * @param nodeSelector The node selector.
 * @param apiCallSection The API call section.
 * @throws IOException If an I/O error occurs.
 */
private static void processStartObjectToken(XContentParser parser, String currentFieldName, Map<String, String> headers, NodeSelector nodeSelector, ApiCallSection apiCallSection) throws IOException {
    XContentParser.Token token;
    if ("headers".equals(currentFieldName)) {
        processHeaders(parser, headers);
    } else if ("node_selector".equals(currentFieldName)) {
        processNodeSelector(parser, nodeSelector);
    } else if (currentFieldName != null) { // must be part of API call then
        processApiCallSection(parser, currentFieldName, apiCallSection);
    }
}
/**
 * This method processes the headers.
 * @param parser The parser to process.
 * @param headers The headers map.
 * @throws IOException If an I/O error occurs.
 */
private static void processHeaders(XContentParser parser, Map<String, String> headers) throws IOException {
    XContentParser.Token token;
    String headerName = null;
    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
        if (token == XContentParser.Token.FIELD_NAME) {
            headerName = parser.currentName();
        } else if (token.isValue()) {
            headers.put(headerName, parser.text());
        }
    }
}
/**
 * This method processes the node selector.
 * @param parser The parser to process.
 * @param nodeSelector The node selector.
 * @throws IOException If an I/O error occurs.
 */
private static void processNodeSelector(XContentParser parser, NodeSelector nodeSelector) throws IOException {
    XContentParser.Token token;
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
}
/**
 * This method processes the API call section.
 * @param parser The parser to process.
 * @param currentFieldName The current field name.
 * @param apiCallSection The API call section.
 * @throws IOException If an I/O error occurs.
 */
private static void processApiCallSection(XContentParser parser, String currentFieldName, ApiCallSection apiCallSection) throws IOException {
    XContentParser.Token token;
    apiCallSection = new ApiCallSection(currentFieldName);
    String paramName = null;
    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
        if (token == XContentParser.Token.FIELD_NAME) {
            paramName = parser.currentName();
        } else if (token.isValue()) {
            processApiCallSectionValue(parser, paramName, apiCallSection);
        } else if (token == XContentParser.Token.START_OBJECT) {
            
  if ("body".equals(paramName)) {
    apiCallSection.addBody(parser.mapOrdered());
  }

        }
    }
}
/**
 * This method processes the API call section value.
 * @param parser The parser to process.
 * @param paramName The parameter name.
 * @param apiCallSection The API call section.
 * @throws IOException If an I/O error occurs.
 */
private static void processApiCallSectionValue(XContentParser parser, String paramName, ApiCallSection apiCallSection) throws IOException {
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
}

/**
 * This method validates the API call section and warnings.
 * @param apiCallSection The API call section to validate.
 * @param expectedWarnings The list of expected warnings.
 * @param expectedWarningsRegex The list of expected warnings regex.
 * @param allowedWarnings The list of allowed warnings.
 * @param allowedWarningsRegex The list of allowed warnings regex.
 */
private static void validateApiCallSectionAndWarnings(ApiCallSection apiCallSection, List<String> expectedWarnings, List<Pattern> expectedWarningsRegex, List<String> allowedWarnings, List<Pattern> allowedWarningsRegex) {
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
}
/**
 * This method sets the API call section and warnings.
 * @param doSection The do section to set.
 * @param apiCallSection The API call section to set.
 * @param headers The headers map.
 * @param nodeSelector The node selector.
 * @param expectedWarnings The list of expected warnings.
 * @param expectedWarningsRegex The list of expected warnings regex.
 * @param allowedWarnings The list of allowed warnings.
 * @param allowedWarningsRegex The list of allowed warnings regex.
 */
private static void setApiCallSectionAndWarnings(DoSection doSection, ApiCallSection apiCallSection, Map<String, String> headers, NodeSelector nodeSelector, List<String> expectedWarnings, List<Pattern> expectedWarningsRegex, List<String> allowedWarnings, List<Pattern> allowedWarningsRegex) {
    apiCallSection.addHeaders(headers);
    apiCallSection.setNodeSelector(nodeSelector);
    doSection.setApiCallSection(apiCallSection);
    doSection.setExpectedWarningHeaders(unmodifiableList(expectedWarnings));
    doSection.setExpectedWarningHeadersRegex(unmodifiableList(expectedWarningsRegex));
    doSection.setAllowedWarningHeaders(unmodifiableList(allowedWarnings));
    doSection.setAllowedWarningHeadersRegex(unmodifiableList(allowedWarningsRegex));
}
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
    throw new IllegalArgumentException("expected [" + XContentParser.Token.START_OBJECT + "], "+ "found ["+ parser.currentToken()+ "], the do section is not properly indented");
  }

    while ((token = parser.nextToken()) != XContentParser.Token.END_OBJECT) {
        if (token == XContentParser.Token.FIELD_NAME) {
            currentFieldName = parser.currentName();
        } else if (token.isValue()) {
            
  if ("catch".equals(currentFieldName)) {
    doSection.setCatch(parser.text());
  }
 else {
    throw new ParsingException(parser.getTokenLocation(),"unsupported field [" + currentFieldName + "]");
  }

        } else if (token == XContentParser.Token.START_ARRAY) {
            processStartArrayToken(parser, currentFieldName, expectedWarnings, expectedWarningsRegex, allowedWarnings, allowedWarningsRegex);
        } else if (token == XContentParser.Token.START_OBJECT) {
            processStartObjectToken(parser, currentFieldName, headers, nodeSelector, apiCallSection);
        }
    }
    try {
        validateApiCallSectionAndWarnings(apiCallSection, expectedWarnings, expectedWarningsRegex, allowedWarnings, allowedWarningsRegex);
        setApiCallSectionAndWarnings(doSection, apiCallSection, headers, nodeSelector, expectedWarnings, expectedWarningsRegex, allowedWarnings, allowedWarningsRegex);
    } finally {
        parser.nextToken();
    }
    return doSection;
}
