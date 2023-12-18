private byte parseByte() throws MDException {
	byte b = 0;
	char c = dmang.getAndIncrement();
	if (Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '$') {
		b = (byte) c;
	}
	else if (c == '?') {
		if ((dmang.peek() == MDMang.DONE)) {
			throw new MDException("MDString parse error: not enough data");
		}
		c = dmang.getAndIncrement();
		if (c >= 'a' && c <= 'z') {
			b = (byte) (c - 'a' + 0xe1);
		}
		else if (c >= 'A' && c <= 'Z') {
			b = (byte) (c - 'A' + 0xc1);
		}
		else {
			switch (c) {
				case '0':
					b = (byte) (',');
					break;
				case '1':
					b = (byte) ('/');
					break;
				case '2':
					b = (byte) ('\\');
					break;
				case '3':
					b = (byte) (':');
					break;
				case '4':
					b = (byte) ('.');
					break;
				case '5':
					b = (byte) (' ');
					break;
				case '6':
					b = (byte) ('\n');
					break;
				case '7':
					b = (byte) ('\t');
					break;
				case '8':
					b = (byte) ('\'');
					break;
				case '9':
					b = (byte) ('-');
					break;
				case '$':
					if ((dmang.peek() == MDMang.DONE)) {
						throw new MDException("MDString parse error: not enough data");
					}
					c = dmang.getAndIncrement();
					if (c < 'A' || c > ('A' + 15)) {
						throw new MDException("MDString parse error: invalid hex code:" + c);
					}
					b = (byte) ((c - 'A') << 4);
					if ((dmang.peek() == MDMang.DONE)) {
						throw new MDException("MDString parse error: not enough data");
					}
					c = dmang.getAndIncrement();
					if (c < 'A' || c > ('A' + 15)) {
						throw new MDException("MDString parse error: invalid hex code:" + c);
					}
					b |= (byte) (c - 'A');
					break;
				default:
					throw new MDException("MDString parse error: invalid code2: " + c);
			}
		}
	}
	else {
		throw new MDException("MDString parse error: invalid code1:" + c);
	}
	return b;
}