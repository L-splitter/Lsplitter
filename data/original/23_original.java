public List<Token> process() {
	while (this.pos < this.max) {
		char ch = this.charsToProcess[this.pos];
		if (isAlphabetic(ch)) {
			lexIdentifier();
		}
		else {
			switch (ch) {
				case '+':
					if (isTwoCharToken(TokenKind.INC)) {
						pushPairToken(TokenKind.INC);
					}
					else {
						pushCharToken(TokenKind.PLUS);
					}
					break;
				case '_': // the other way to start an identifier
					lexIdentifier();
					break;
				case '-':
					if (isTwoCharToken(TokenKind.DEC)) {
						pushPairToken(TokenKind.DEC);
					}
					else {
						pushCharToken(TokenKind.MINUS);
					}
					break;
				case ':':
					pushCharToken(TokenKind.COLON);
					break;
				case '.':
					pushCharToken(TokenKind.DOT);
					break;
				case ',':
					pushCharToken(TokenKind.COMMA);
					break;
				case '*':
					pushCharToken(TokenKind.STAR);
					break;
				case '/':
					pushCharToken(TokenKind.DIV);
					break;
				case '%':
					pushCharToken(TokenKind.MOD);
					break;
				case '(':
					pushCharToken(TokenKind.LPAREN);
					break;
				case ')':
					pushCharToken(TokenKind.RPAREN);
					break;
				case '[':
					pushCharToken(TokenKind.LSQUARE);
					break;
				case '#':
					pushCharToken(TokenKind.HASH);
					break;
				case ']':
					pushCharToken(TokenKind.RSQUARE);
					break;
				case '{':
					pushCharToken(TokenKind.LCURLY);
					break;
				case '}':
					pushCharToken(TokenKind.RCURLY);
					break;
				case '@':
					pushCharToken(TokenKind.BEAN_REF);
					break;
				case '^':
					if (isTwoCharToken(TokenKind.SELECT_FIRST)) {
						pushPairToken(TokenKind.SELECT_FIRST);
					}
					else {
						pushCharToken(TokenKind.POWER);
					}
					break;
				case '!':
					if (isTwoCharToken(TokenKind.NE)) {
						pushPairToken(TokenKind.NE);
					}
					else if (isTwoCharToken(TokenKind.PROJECT)) {
						pushPairToken(TokenKind.PROJECT);
					}
					else {
						pushCharToken(TokenKind.NOT);
					}
					break;
				case '=':
					if (isTwoCharToken(TokenKind.EQ)) {
						pushPairToken(TokenKind.EQ);
					}
					else {
						pushCharToken(TokenKind.ASSIGN);
					}
					break;
				case '&':
					if (isTwoCharToken(TokenKind.SYMBOLIC_AND)) {
						pushPairToken(TokenKind.SYMBOLIC_AND);
					}
					else {
						pushCharToken(TokenKind.FACTORY_BEAN_REF);
					}
					break;
				case '|':
					if (!isTwoCharToken(TokenKind.SYMBOLIC_OR)) {
						raiseParseException(this.pos, SpelMessage.MISSING_CHARACTER, "|");
					}
					pushPairToken(TokenKind.SYMBOLIC_OR);
					break;
				case '?':
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
					break;
				case '$':
					if (isTwoCharToken(TokenKind.SELECT_LAST)) {
						pushPairToken(TokenKind.SELECT_LAST);
					}
					else {
						lexIdentifier();
					}
					break;
				case '>':
					if (isTwoCharToken(TokenKind.GE)) {
						pushPairToken(TokenKind.GE);
					}
					else {
						pushCharToken(TokenKind.GT);
					}
					break;
				case '<':
					if (isTwoCharToken(TokenKind.LE)) {
						pushPairToken(TokenKind.LE);
					}
					else {
						pushCharToken(TokenKind.LT);
					}
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
					// drift over white space
					this.pos++;
					break;
				case '\'':
					lexQuotedStringLiteral();
					break;
				case '"':
					lexDoubleQuotedStringLiteral();
					break;
				case 0:
					// hit sentinel at end of value
					this.pos++;  // will take us to the end
					break;
				case '\\':
					raiseParseException(this.pos, SpelMessage.UNEXPECTED_ESCAPE_CHAR);
					break;
				default:
					throw new IllegalStateException(
							"Unsupported character '%s' (%d) encountered at position %d in expression."
									.formatted(ch, (int) ch, (this.pos + 1)));
			}
		}
	}
	return this.tokens;
}