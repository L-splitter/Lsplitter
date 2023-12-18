public PathPattern parse(String pathPattern) throws PatternParseException {
	Assert.notNull(pathPattern, "Path pattern must not be null");

	this.pathPatternData = pathPattern.toCharArray();
	this.pathPatternLength = this.pathPatternData.length;
	this.headPE = null;
	this.currentPE = null;
	this.capturedVariableNames = null;
	this.pathElementStart = -1;
	this.pos = 0;
	resetPathElementState();

	while (this.pos < this.pathPatternLength) {
		char ch = this.pathPatternData[this.pos];
		char separator = this.parser.getPathOptions().separator();
		if (ch == separator) {
			if (this.pathElementStart != -1) {
				pushPathElement(createPathElement());
			}
			if (peekDoubleWildcard()) {
				pushPathElement(new WildcardTheRestPathElement(this.pos, separator));
				this.pos += 2;
			}
			else {
				pushPathElement(new SeparatorPathElement(this.pos, separator));
			}
		}
		else {
			if (this.pathElementStart == -1) {
				this.pathElementStart = this.pos;
			}
			if (ch == '?') {
				this.singleCharWildcardCount++;
			}
			else if (ch == '{') {
				if (this.insideVariableCapture) {
					throw new PatternParseException(this.pos, this.pathPatternData,
							PatternMessage.ILLEGAL_NESTED_CAPTURE);
				}
				// If we enforced that adjacent captures weren't allowed,
				// this would do it (this would be an error: /foo/{bar}{boo}/)
				// } else if (pos > 0 && pathPatternData[pos - 1] == '}') {
				// throw new PatternParseException(pos, pathPatternData,
				// PatternMessage.CANNOT_HAVE_ADJACENT_CAPTURES);
				this.insideVariableCapture = true;
				this.variableCaptureStart = this.pos;
			}
			else if (ch == '}') {
				if (!this.insideVariableCapture) {
					throw new PatternParseException(this.pos, this.pathPatternData,
							PatternMessage.MISSING_OPEN_CAPTURE);
				}
				this.insideVariableCapture = false;
				if (this.isCaptureTheRestVariable && (this.pos + 1) < this.pathPatternLength) {
					throw new PatternParseException(this.pos + 1, this.pathPatternData,
							PatternMessage.NO_MORE_DATA_EXPECTED_AFTER_CAPTURE_THE_REST);
				}
				this.variableCaptureCount++;
			}
			else if (ch == ':') {
				if (this.insideVariableCapture && !this.isCaptureTheRestVariable) {
					skipCaptureRegex();
					this.insideVariableCapture = false;
					this.variableCaptureCount++;
				}
			}
			else if (ch == '*') {
				if (this.insideVariableCapture && this.variableCaptureStart == this.pos - 1) {
					this.isCaptureTheRestVariable = true;
				}
				this.wildcard = true;
			}
			// Check that the characters used for captured variable names are like java identifiers
			if (this.insideVariableCapture) {
				if ((this.variableCaptureStart + 1 + (this.isCaptureTheRestVariable ? 1 : 0)) == this.pos &&
						!Character.isJavaIdentifierStart(ch)) {
					throw new PatternParseException(this.pos, this.pathPatternData,
							PatternMessage.ILLEGAL_CHARACTER_AT_START_OF_CAPTURE_DESCRIPTOR,
							Character.toString(ch));

				}
				else if ((this.pos > (this.variableCaptureStart + 1 + (this.isCaptureTheRestVariable ? 1 : 0)) &&
						!Character.isJavaIdentifierPart(ch) && ch != '-')) {
					throw new PatternParseException(this.pos, this.pathPatternData,
							PatternMessage.ILLEGAL_CHARACTER_IN_CAPTURE_DESCRIPTOR,
							Character.toString(ch));
				}
			}
		}
		this.pos++;
	}
	if (this.pathElementStart != -1) {
		pushPathElement(createPathElement());
	}
	return new PathPattern(pathPattern, this.parser, this.headPE);
}