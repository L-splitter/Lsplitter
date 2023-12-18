/**
 * This method parses a byte from the dmang object.
 * @return byte
 * @throws MDException
 */
private byte parseByte() throws MDException {
    byte b = 0;
    char c = dmang.getAndIncrement();
    if (( Character.isLetter(c) || Character.isDigit(c) || c == '_' || c == '$')) {
        b = (byte) c;
    } else if (c == '?') {
        b = handleSpecialCharacter();
    } else {
        throw new MDException("MDString parse error: invalid code1:" + c);
    }
    return b;
}

/**
 * This method handles special characters.
 * @return byte
 * @throws MDException
 */
private byte handleSpecialCharacter() throws MDException {
    byte b;
    checkDataAvailability();
    char c = dmang.getAndIncrement();
    if (isLowerCaseLetter(c)) {
        b = (byte) (c - 'a' + 0xe1);
    } else if (isUpperCaseLetter(c)) {
        b = (byte) (c - 'A' + 0xc1);
    } else {
        b = handleOtherCharacters(c);
    }
    return b;
}
/**
 * This method checks if the character is a lower case letter.
 * @param c character to check
 * @return boolean
 */
private boolean isLowerCaseLetter(char c) {
    return c >= 'a' && c <= 'z';
}
/**
 * This method checks if the character is an upper case letter.
 * @param c character to check
 * @return boolean
 */
private boolean isUpperCaseLetter(char c) {
    return c >= 'A' && c <= 'Z';
}
/**
 * This method handles other characters.
 * @param c character to handle
 * @return byte
 * @throws MDException
 */
private byte handleOtherCharacters(char c) throws MDException {
    byte b;
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
            b = handleDollarCharacter();
            break;
        default:
            throw new MDException("MDString parse error: invalid code2: " + c);
    }
    return b;
}
/**
 * This method handles the dollar character.
 * @return byte
 * @throws MDException
 */
private byte handleDollarCharacter() throws MDException {
    byte b;
    checkDataAvailability();
    char c = dmang.getAndIncrement();
    checkHexCode(c);
    b = (byte) ((c - 'A') << 4);
    checkDataAvailability();
    c = dmang.getAndIncrement();
    checkHexCode(c);
    b |= (byte) (c - 'A');
    return b;
}
/**
 * This method checks if there is enough data available.
 * @throws MDException
 */
private void checkDataAvailability() throws MDException {
    if ((dmang.peek() == MDMang.DONE)) {
        throw new MDException("MDString parse error: not enough data");
    }
}
/**
 * This method checks if the hex code is valid.
 * @param c character to check
 * @throws MDException
 */
private void checkHexCode(char c) throws MDException {
    if (c < 'A' || c > ('A' + 15)) {
        throw new MDException("MDString parse error: invalid hex code:" + c);
    }
}
