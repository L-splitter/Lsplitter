/**
 * This method processes the tag.
 *
 * @param tagType       The type of the tag.
 * @param tagBody       The body of the tag.
 * @param file          The file path.
 * @param lineNum       The line number.
 * @param tagProcessor  The tag processor.
 * @return              The processed tag.
 * @throws IOException  If an I/O error occurs.
 */
private static String processTag(String tagType, String tagBody, Path file, int lineNum,
                                 TagProcessor tagProcessor) throws IOException {
    
  if (tagBody.indexOf('<') >= 0 || tagBody.indexOf('>') >= 0) {
    throw new IOException("Bad Tag at line " + lineNum);
  }

    LinkedHashMap<String, String> map = new LinkedHashMap<>();
    StringBuffer buf = new StringBuffer();
    String attr = null;
    TagProcessingState mode = LOOKING_FOR_NEXT_ATTR;
    char term = 0;
    processTagBody(tagBody, map, buf, mode, attr, term);
    tagProcessor.processTag(tagType, map, file, lineNum);
    return buildTagString(tagType, map);
}

/**
 * This method processes the tag body.
 *
 * @param tagBody  The body of the tag.
 * @param map      The map to store the attributes and values.
 * @param buf      The buffer to store the attribute or value.
 * @param mode     The current processing state.
 * @param attr     The current attribute.
 * @param term     The termination character.
 */
private static void processTagBody(String tagBody, LinkedHashMap<String, String> map, StringBuffer buf,
                                   TagProcessingState mode, String attr, char term) {
    int end = tagBody.length();
    for (int ix = 0; ix < end; ix++) {
        char c = tagBody.charAt(ix);
        switch (mode) {
            case READING_ATTR:
                mode = processReadingAttr(c, map, buf, attr);
                break;
            case LOOKING_FOR_VALUE:
                mode = processLookingForValue(c, buf);
                break;
            case READING_VALUE:
                mode = processReadingValue(c, map, buf, attr, term);
                break;
            default:
                mode = processDefault(c, buf);
        }
    }
    processEndState(mode, map, buf, attr);
}
/**
 * This method processes the reading attribute state.
 *
 * @param c     The current character.
 * @param map   The map to store the attributes and values.
 * @param buf   The buffer to store the attribute.
 * @param attr  The current attribute.
 * @return      The next processing state.
 */
private static TagProcessingState processReadingAttr(char c, LinkedHashMap<String, String> map, StringBuffer buf,
                                                     String attr) {
    if (c == '=') {
        attr = buf.toString().toLowerCase();
        return LOOKING_FOR_VALUE;
    }
    if (c == ' ' || c == '\t') {
        attr = buf.toString().toLowerCase();
        map.put(attr, null);
        return LOOKING_FOR_NEXT_ATTR;
    }
    buf.append(c);
    return READING_ATTR;
}
/**
 * This method processes the looking for value state.
 *
 * @param c   The current character.
 * @param buf The buffer to store the value.
 * @return    The next processing state.
 */
private static TagProcessingState processLookingForValue(char c, StringBuffer buf) {
    if (c == ' ' || c == '\t') {
        return LOOKING_FOR_VALUE;
    }
    if (c == '"' || c == '\'') {
        buf = new StringBuffer();
        return READING_VALUE;
    }
    buf = new StringBuffer();
    buf.append(c);
    return READING_VALUE;
}
/**
 * This method processes the reading value state.
 *
 * @param c     The current character.
 * @param map   The map to store the attributes and values.
 * @param buf   The buffer to store the value.
 * @param attr  The current attribute.
 * @param term  The termination character.
 * @return      The next processing state.
 */
private static TagProcessingState processReadingValue(char c, LinkedHashMap<String, String> map, StringBuffer buf,
                                                      String attr, char term) {
    if (c == term || (term == 0 && (c == ' ' || c == '\t'))) {
        map.put(attr, buf.toString());
        return LOOKING_FOR_NEXT_ATTR;
    }
    buf.append(c);
    return READING_VALUE;
}
/**
 * This method processes the default state.
 *
 * @param c   The current character.
 * @param buf The buffer to store the attribute.
 * @return    The next processing state.
 */
private static TagProcessingState processDefault(char c, StringBuffer buf) {
    if (c == ' ' || c == '\t') {
        return LOOKING_FOR_NEXT_ATTR;
    }
    buf = new StringBuffer();
    buf.append(c);
    return READING_ATTR;
}
/**
 * This method processes the end state.
 *
 * @param mode The current processing state.
 * @param map  The map to store the attributes and values.
 * @param buf  The buffer to store the attribute or value.
 * @param attr The current attribute.
 */
private static void processEndState(TagProcessingState mode, LinkedHashMap<String, String> map, StringBuffer buf,
                                    String attr) {
    if (mode == READING_ATTR) {
        map.put(buf.toString().toLowerCase(), null);
    } else if (mode == LOOKING_FOR_VALUE) {
        map.put(attr, null);
    } else if (mode == READING_VALUE) {
        map.put(attr, buf.toString());
    }
}
/**
 * This method builds the tag string.
 *
 * @param tagType The type of the tag.
 * @param map     The map of attributes and values.
 * @return        The tag string.
 */
private static String buildTagString(String tagType, LinkedHashMap<String, String> map) {
    StringBuffer buf = new StringBuffer();
    buf.append('<');
    buf.append(tagType);
    Iterator<String> iter = map.keySet().iterator();
    while (iter.hasNext()) {
        String attr = iter.next();
        String value = map.get(attr);
        buf.append(' ');
        buf.append(attr);
        if (value != null) {
            buf.append("=\"");
            buf.append(value);
            buf.append("\"");
        }
    }
    buf.append('>');
    return buf.toString();
}
