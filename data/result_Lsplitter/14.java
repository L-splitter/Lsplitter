/**
 * This method is used to parse the path pattern.
 * @param pathPattern the path pattern to be parsed
 * @return the parsed path pattern
 * @throws PatternParseException if the pattern is not valid
 */
public PathPattern parse(String pathPattern) throws PatternParseException {
    initialize(pathPattern);
    while (this.pos < this.pathPatternLength) {
        char ch = this.pathPatternData[this.pos];
        char separator = this.parser.getPathOptions().separator();
        if (ch == separator) {
            handleSeparator(ch, separator);
        } else {
            handleNonSeparator(ch);
        }
        this.pos++;
    }
    if (this.pathElementStart != -1) {
        pushPathElement(createPathElement());
    }
    return new PathPattern(pathPattern, this.parser, this.headPE);
}
/**
 * This method is used to initialize the variables.
 * @param pathPattern the path pattern to be parsed
 */
private void initialize(String pathPattern) {
    Assert.notNull(pathPattern, "Path pattern must not be null");
    this.pathPatternData = pathPattern.toCharArray();
    this.pathPatternLength = this.pathPatternData.length;
    this.headPE = null;
    this.currentPE = null;
    this.capturedVariableNames = null;
    this.pathElementStart = -1;
    this.pos = 0;
    resetPathElementState();
}
/**
 * This method is used to handle the separator character.
 * @param ch the current character
 * @param separator the separator character
 */
private void handleSeparator(char ch, char separator) {
    if (this.pathElementStart != -1) {
        pushPathElement(createPathElement());
    }
    if (peekDoubleWildcard()) {
        pushPathElement(new WildcardTheRestPathElement(this.pos, separator));
        this.pos += 2;
    } else {
        pushPathElement(new SeparatorPathElement(this.pos, separator));
    }
}
/**
 * This method is used to handle the non-separator character.
 * @param ch the current character
 */
private void handleNonSeparator(char ch) {
    if (this.pathElementStart == -1) {
        this.pathElementStart = this.pos;
    }
    if (ch == '?') {
        this.singleCharWildcardCount++;
    } else if (ch == '{') {
        handleOpenBracket();
    } else if (ch == '}') {
        handleCloseBracket();
    } else if (ch == ':') {
        handleColon();
    } else if (ch == '*') {
        
  if (this.insideVariableCapture && this.variableCaptureStart == this.pos - 1) {
    this.isCaptureTheRestVariable=true;
  }
  this.wildcard=true;

    }
    if (this.insideVariableCapture) {
        
  if ((this.variableCaptureStart + 1 + (this.isCaptureTheRestVariable ? 1 : 0)) == this.pos && !Character.isJavaIdentifierStart(ch)) {
    throw new PatternParseException(this.pos,this.pathPatternData,PatternMessage.ILLEGAL_CHARACTER_AT_START_OF_CAPTURE_DESCRIPTOR,Character.toString(ch));
  }
 else   if ((this.pos > (this.variableCaptureStart + 1 + (this.isCaptureTheRestVariable ? 1 : 0)) && !Character.isJavaIdentifierPart(ch) && ch != '-')) {
    throw new PatternParseException(this.pos,this.pathPatternData,PatternMessage.ILLEGAL_CHARACTER_IN_CAPTURE_DESCRIPTOR,Character.toString(ch));
  }

    }
}
/**
 * This method is used to handle the open bracket character.
 */
private void handleOpenBracket() {
    if (this.insideVariableCapture) {
        throw new PatternParseException(this.pos, this.pathPatternData,
                PatternMessage.ILLEGAL_NESTED_CAPTURE);
    }
    this.insideVariableCapture = true;
    this.variableCaptureStart = this.pos;
}
/**
 * This method is used to handle the close bracket character.
 */
private void handleCloseBracket() {
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
/**
 * This method is used to handle the colon character.
 */
private void handleColon() {
    if (this.insideVariableCapture && !this.isCaptureTheRestVariable) {
        skipCaptureRegex();
        this.insideVariableCapture = false;
        this.variableCaptureCount++;
    }
}
