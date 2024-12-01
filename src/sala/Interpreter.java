package sala;

import org.jetbrains.annotations.NotNull;

import java.util.*;

public class Interpreter {
    static class LineLexer {
        char[] text;
        int idx;
        int ln;
        char ch;

        LineLexer(@NotNull String text, int ln) {
            this.text = text.toCharArray();
            idx = -1;
            this.ln = ln;
            ch = 0;
            this.advance();
        }

        private void advance() {
            idx++;
            ch = idx < text.length ? text[idx] : 0;
        }

        private Character peek() {
            return idx - 1 > -1 ? text[idx - 1] : 0;
        }

        private boolean notEnded() {
            return ch != 0 && ch != '\n';
        }

        private boolean whitespace() {
            return ch == '\t' || ch == '\r' || ch == ' ';
        }

        private boolean numChar() {
            return ch >= '0' && ch <= '9';
        }

        private boolean wordChar() {
            return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') || numChar() || ch == '_';
        }

        private Token collectWord(int mode) {
            var start = idx;
            var str = new StringBuilder();

            if (mode == 1 || mode == 2) advance();

            while (notEnded() && wordChar()) {
                str.append(ch);
                advance();
            }

            if (mode == 1) {
                return new Token(TokenTypes.DIRECTIVE, str.toString(), start, ln);
            } else if (mode == 2) {
                return new Token(TokenTypes.LABEL, str.toString(), start, ln);
            } else if (mode == 3) {
                return new Token(TokenTypes.ALIAS, str.toString(), start, ln);
            }

            return new Token(TokenTypes.WORD, str.toString(), start, ln);
        }

        private Token collectNumber() {
            var start = idx;
            var str = new StringBuilder();

            while (notEnded() && numChar()) {
                str.append(ch);
                advance();
            }

            return new Token(TokenTypes.NUMBER, str.toString(), start, ln);
        }

        private Token collectString() throws SalaException {
            var start = idx;
            var str = new StringBuilder();

            advance();

            var escaped = false;
            while (notEnded()) {
                if (escaped) {
                    var c = switch (ch) {
                        case '\\', '\'', '"' -> ch;
                        case 'n' -> '\n';
                        case 'r' -> '\r';
                        case '0' -> '\0';
                        default -> throw new SalaException(String.format("error on line %d, col %d: invalid escape character '%c'", ln + 1, idx + 1, ch));
                    };
                    str.append(c);
                    escaped = false;
                    advance();
                } else if (ch == '\\') {
                    escaped = true;
                    advance();
                } else if (ch == '"') {
                    break;
                } else {
                    str.append(ch);
                    advance();
                }
            }

            if (ch != '"') {
                throw new SalaException(String.format("error on line %d, col %d: unterminated string literal", ln + 1, start + 1));
            }

            advance();

            return new Token(TokenTypes.STRING, str.toString(), start, ln);
        }

        public @NotNull Token[] lex() throws SalaException {
            var tokens = new ArrayList<Token>();

            while (notEnded()) {
                if (whitespace()) {
                    advance();
                } else if (ch == '"') {
                    tokens.add(collectString());
                } else if (numChar()) {
                    tokens.add(collectNumber());
                } else if (ch == '.') {
                    tokens.add(collectWord(1));
                } else if (ch == ':') {
                    tokens.add(collectWord(2));
                } else if (ch == '$') {
                    tokens.add(collectWord(3));
                }else if (wordChar()) {
                    tokens.add(collectWord(0));
                } else if (ch == ';') {
                    break;
                } else {
                    throw new SalaException(String.format("error on line %d, col %d: illegal character '%c'", ln + 1, idx + 1, ch));
                }
            }

            return tokens.toArray(new Token[tokens.size()]);
        }
    }

    private final @NotNull SalaOutput out;
    private final @NotNull  SalaInput in;

    public static @NotNull Token[][] lexLines(@NotNull String text) throws SalaException {
        var tlines = new ArrayList<Token[]>();

        var ln = 0;
        for (String line : text.split("\n")) {
            var lexer = new LineLexer(line, ln);
            tlines.add(lexer.lex());
            ln++;
        }

        return tlines.toArray(new Token[tlines.size()][]);
    }

    public Interpreter(@NotNull SalaOutput out, @NotNull SalaInput in) {
        this.out = out;
        this.in = in;
    }

    public Interpreter(@NotNull SalaInput in) {
        this.out = System.out::print;
        this.in = in;
    }

    public void interpret(@NotNull Token[][] tokens) throws SalaException {
        var stack = new SalaStack();
        var vars = new Object[0];
        var afterContent = false;
        var labels = new HashMap<String, Integer>();
        var directives = new HashMap<String, SalaEntry>((Map.of(

        )));
        var aliasMap = new HashMap<String, Token>();
        var instructions = StandardInstructions.get(stack, vars, out, labels);

        {
            var ln = 0;
            for (Token[] line : tokens) {
                if (line.length != 0) {
                    if (line[0].is(TokenTypes.LABEL)) {
                        if (labels.get(line[0].getLiteral()) != null) {
                            throw new SalaException(line[0].err("label '%s' already defined", line[0].getLiteral()));
                        } else if (line.length > 1) {
                            throw new SalaException(line[0].err("label is not alone on the line"));
                        } else if (Objects.equals(line[0].getLiteral(), "")) {
                            throw new SalaException(line[0].err("labels cannot be empty"));
                        }
                        labels.put(line[0].getLiteral(), ln);
                    } else if (line[0].is(TokenTypes.WORD, "alias")) {
                        var cmp = new Token[line.length - 1];
                        System.arraycopy(line, 1, cmp, 0, line.length - 1);
                        var aliasInstr = new Instruction().expect(TokenTypes.WORD).expect(new TokenTypes[]{TokenTypes.NUMBER, TokenTypes.STRING, TokenTypes.WORD, TokenTypes.LABEL, TokenTypes.DIRECTIVE, TokenTypes.ALIAS});
                        aliasInstr.check(cmp);
                        aliasMap.put(cmp[0].getLiteral(), cmp[1]);
                    } else {
                        var i = 0;
                        for (Token t : line) {
                            if (t.is(TokenTypes.ALIAS)) tokens[ln][i] = t.fromAlias(aliasMap);
                            i++;
                        }
                    }
                }
                ln++;
            }
        }

        for (int i = 0; i < tokens.length; i++) {
            // System.out.println(stack);
            var line = tokens[i];
            if (line.length == 0) continue;
            var t = line[0];

            if (t.is(TokenTypes.LABEL)) {
                afterContent = true;
            } else if (t.is(TokenTypes.DIRECTIVE)) {
                if (afterContent) throw new SalaException(t.err("directives can only be used at the top of the file before any code"));

                var d = directives.get(t.getLiteral());
                if (d == null) throw new SalaException(t.err("unknown directive '%s'", t.getLiteral()));

                var cmp = new Token[line.length - 1];
                System.arraycopy(line, 1, cmp, 0, line.length - 1);
                d.instruction.check(cmp);
                var r = new Ref<>(i);
                d.effect.happen(r, t, cmp);
                i = r.value;
            } else if (t.is(TokenTypes.WORD)) {
                if (t.is("alias")) continue;
                afterContent = true;

                var ins = instructions.get(t.getLiteral());
                if (ins == null) throw new SalaException(t.err("unknown instruction '%s'", t.getLiteral()));

                var cmp = new Token[line.length - 1];
                System.arraycopy(line, 1, cmp, 0, line.length - 1);
                ins.instruction.check(cmp);
                var r = new Ref<>(i);
                ins.effect.happen(r, t, cmp);
                i = r.value;
            } else {
                throw new SalaException(t.err("unexpected %s", t.getType().name().toLowerCase()));
            }
        }
    }

    public void interpret(@NotNull String text) throws SalaException {
        interpret(lexLines(text));
    }
}
