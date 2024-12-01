package sala;

import java.util.Map;
import java.util.Objects;

public class Token {
    private final TokenTypes type;
    private final String literal;
    private final int start, ln;

    public Token(TokenTypes type, String literal, int start, int ln) {
        this.type = type;
        this.literal = literal;
        this.start = start;
        this.ln = ln;
    }

    public Token(TokenTypes type, String literal) {
        this(type, literal, -1, -1);
    }

    public Token() {
        this(TokenTypes.NONE, "");
    }

    public boolean is(TokenTypes type, String literal) {
        return this.type == type && Objects.equals(this.literal, literal);
    }

    public boolean is(TokenTypes type) {
        return this.type == type;
    }

    public boolean is(String literal) {
        return Objects.equals(this.literal, literal);
    }

    public String pos() {
        return String.format("line %d, col %d", ln + 1, start + 1);
    }

    public String err() {
        return "error on " + pos() + ":";
    }

    public String err(String msg) {
        return err() + " " + msg;
    }

    public String err(String format, Object... args) {
        return err() + " " + String.format(format, args);
    }

    public Token fromAlias(Map<String, Token> aliasMap) throws SalaException {
        if (type != TokenTypes.ALIAS) throw new SalaException(err("token is not an alias token"));
        var result = aliasMap.get(literal);
        if (result == null) throw new SalaException(err("unknown alias '%s'", literal));
        return result;
    }

    public String getLiteral() {
        return literal;
    }

    public TokenTypes getType() {
        return type;
    }

    @Override
    public String toString() {
        return String.format("Token{%s, '%s', %d, %d}", type.toString(), literal, start, ln);
    }
}
