/**
 * Main method to process the tokens.
 * @return List of processed tokens.
 */
public List<Token> process() {
	while (this.pos < this.max) {
		char ch = this.charsToProcess[this.pos];
		if (isAlphabetic(ch)) {
			lexIdentifier();
		}
		else {
			processNonAlphabetic(ch);
		}
	}
	return this.tokens;
}
/**
 * Method to process non-alphabetic characters.
 * @param ch Character to process.
 */
private void processNonAlphabetic(char ch) {
	switch (ch) {
		case '+':
		case '-':
		case '^':
		case '!':
		case '=':
		case '&':
		case '|':
		case '?':
		case '>':
		case '<':
			processTwoCharToken(ch);
			break;
		case '_':
		case '$':
			lexIdentifier();
			break;
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			lexNumericLiteral(ch == '0');
			break;
		case ' ':
		case '\t':
		case '\r':
		case '\n':
			this.pos++;
			break;
		case '\'':
			lexQuotedStringLiteral();
			break;
		case '"':
			lexDoubleQuotedStringLiteral();
			break;
		case 0:
			this.pos++;
			break;
		case '\\':
			raiseParseException(this.pos, SpelMessage.UNEXPECTED_ESCAPE_CHAR);
			break;
		default:
			processDefaultChar(ch);
	}
}
/**
 * Method to process two character tokens.
 * @param ch Character to process.
 */
private void processTwoCharToken(char ch) {
	switch (ch) {
		case '+':
			processToken(TokenKind.INC, TokenKind.PLUS);
			break;
		case '-':
			processToken(TokenKind.DEC, TokenKind.MINUS);
			break;
		case '^':
			processToken(TokenKind.SELECT_FIRST, TokenKind.POWER);
			break;
		case '!':
			processExclamationToken();
			break;
		case '=':
			processToken(TokenKind.EQ, TokenKind.ASSIGN);
			break;
		case '&':
			processToken(TokenKind.SYMBOLIC_AND, TokenKind.FACTORY_BEAN_REF);
			break;
		case '|':
			processPipeToken();
			break;
		case '?':
			processQuestionToken();
			break;
		case '>':
			processToken(TokenKind.GE, TokenKind.GT);
			break;
		case '<':
			processToken(TokenKind.LE, TokenKind.LT);
			break;
	}
}
/**
 * Method to process exclamation token.
 */
private void processExclamationToken() {
	if (isTwoCharToken(TokenKind.NE)) {
		pushPairToken(TokenKind.NE);
	}
	else if (isTwoCharToken(TokenKind.PROJECT)) {
		pushPairToken(TokenKind.PROJECT);
	}
	else {
		pushCharToken(TokenKind.NOT);
	}
}
/**
 * Method to process pipe token.
 */
private void processPipeToken() {
	if (!isTwoCharToken(TokenKind.SYMBOLIC_OR)) {
		raiseParseException(this.pos, SpelMessage.MISSING_CHARACTER, "|");
	}
	pushPairToken(TokenKind.SYMBOLIC_OR);
}
/**
 * Method to process question token.
 */
private void processQuestionToken() {
	if (isTwoCharToken(TokenKind.SELECT)) {
		pushPairToken(TokenKind.SELECT);
	}
	else if (isTwoCharToken(TokenKind.ELVIS)) {
		pushPairToken(TokenKind.ELVIS);
	}
	else if (isTwoCharToken(TokenKind.SAFE_NAVI)) {
		pushPairToken(TokenKind.SAFE_NAVI);
	}
	else {
		pushCharToken(TokenKind.QMARK);
	}
}
/**
 * Method to process default character.
 * @param ch Character to process.
 */
private void processDefaultChar(char ch) {
	throw new IllegalStateException(
			"Unsupported character '%s' (%d) encountered at position %d in expression."
					.formatted(ch, (int) ch, (this.pos + 1)));
}
/**
 * Method to process token.
 * @param twoCharToken Two character token kind.
 * @param singleCharToken Single character token kind.
 */
private void processToken(TokenKind twoCharToken, TokenKind singleCharToken) {
	if (isTwoCharToken(twoCharToken)) {
		pushPairToken(twoCharToken);
	}
	else {
		pushCharToken(singleCharToken);
	}
}
