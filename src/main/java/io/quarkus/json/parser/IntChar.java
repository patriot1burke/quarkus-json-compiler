package io.quarkus.json.parser;

public class IntChar {
    final static int INT_TAB = '\t';
    final static int INT_LF = '\n';
    final static int INT_CR = '\r';
    final static int INT_SPACE = 0x0020;

    // Markup
    final static int INT_LBRACKET = '[';
    final static int INT_RBRACKET = ']';
    final static int INT_LCURLY = '{';
    final static int INT_RCURLY = '}';
    final static int INT_QUOTE = '"';
    final static int INT_APOS = '\'';
    final static int INT_BACKSLASH = '\\';
    final static int INT_SLASH = '/';
    final static int INT_ASTERISK = '*';
    final static int INT_COLON = ':';
    final static int INT_COMMA = ',';
    final static int INT_HASH = '#';

    // Number chars
    final static int INT_0 = '0';
    final static int INT_9 = '9';
    final static int INT_MINUS = '-';
    final static int INT_PLUS = '+';

    final static int INT_PERIOD = '.';
    final static int INT_e = 'e';
    final static int INT_E = 'E';

    final static int INT_t = 't';
    final static int INT_f = 'f';


    public static boolean isDigit(int ch) {
        return ch >=INT_0 && ch <= INT_9;
    }

    public static boolean isWhitespace(int ch) {
        return ch == IntChar.INT_SPACE
            || ch == IntChar.INT_LF
                || ch == IntChar.INT_CR
                  || ch == IntChar.INT_TAB;
    }
}
