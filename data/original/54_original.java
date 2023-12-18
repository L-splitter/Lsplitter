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

	int end = tagBody.length();
	for (int ix = 0; ix < end; ix++) {
		char c = tagBody.charAt(ix);

		switch (mode) {

			case READING_ATTR:
				if (c == '=') {
					attr = buf.toString().toLowerCase();
					mode = LOOKING_FOR_VALUE;
					break;
				}
				if (c == ' ' || c == '\t') {
					attr = buf.toString().toLowerCase();
					map.put(attr, null);
					mode = LOOKING_FOR_NEXT_ATTR;
					break;
				}
				buf.append(c);
				break;

			case LOOKING_FOR_VALUE:
				if (c == ' ' || c == '\t') {
					// we now allow spaces after the '=', but before the '"' starts, as our 
					// tidy tool breaks on the '=' sometimes
					//map.put(attr, null);
					//mode = LOOKING_FOR_NEXT_ATTR;
					break;
				}
				if (c == '"' || c == '\'') {
					buf = new StringBuffer();
					mode = READING_VALUE;
					term = c;
					break;
				}
				buf = new StringBuffer();
				buf.append(c);
				mode = READING_VALUE;
				term = 0;
				break;

			case READING_VALUE:
				if (c == term || (term == 0 && (c == ' ' || c == '\t'))) {
					map.put(attr, buf.toString());
					mode = LOOKING_FOR_NEXT_ATTR;
					break;
				}
				buf.append(c);
				break;

			default:
				if (c == ' ' || c == '\t') {
					continue;
				}
				buf = new StringBuffer();
				buf.append(c);
				mode = READING_ATTR;
		}
	}

	if (mode == READING_ATTR) {
		map.put(buf.toString().toLowerCase(), null);
	}
	else if (mode == LOOKING_FOR_VALUE) {
		map.put(attr, null);
	}
	else if (mode == READING_VALUE) {
		map.put(attr, buf.toString());
	}

	tagProcessor.processTag(tagType, map, file, lineNum);

	buf = new StringBuffer();
	buf.append('<');
	buf.append(tagType);
	Iterator<String> iter = map.keySet().iterator();
	while (iter.hasNext()) {
		attr = iter.next();
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