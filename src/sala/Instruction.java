package sala;

import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class Instruction {
    private static class ExpectedToken {
        private final TokenTypes[] expectedTypes;
        private final String[] expectedLiterals;
        private boolean trailing;

        public ExpectedToken(TokenTypes[] types, String[] literals) {
            this.expectedTypes = types;
            this.expectedLiterals = literals;
        }

        public ExpectedToken(TokenTypes[] type, String literal) {
            this(type, new String[]{literal});
        }

        public ExpectedToken(TokenTypes type, String[] literals) {
            this(new TokenTypes[]{type}, literals);
        }

        public ExpectedToken(TokenTypes type, String literal) {
            this(new TokenTypes[]{type}, new String[]{literal});
        }

        public ExpectedToken(TokenTypes type) {
            this(type, new String[0]);
        }

        private String formatTypes() {
            if (expectedTypes.length == 1) {
                return expectedTypes[0].name().toLowerCase();
            } else if (expectedTypes.length == 2) {
                return expectedTypes[0].name().toLowerCase() + "' or '" + expectedTypes[1].name().toLowerCase();
            }

            var s = new StringBuilder();

            for (int i = 0; i < expectedTypes.length; i++) {
                if (i == expectedTypes.length - 1) {
                    s.append(expectedTypes[i].name().toLowerCase());
                } else if (i == expectedTypes.length - 2 && expectedTypes.length - 2 != 0) {
                    s.append(expectedTypes[i].name().toLowerCase()).append("', or '");
                } else {
                    s.append(expectedTypes[i].name().toLowerCase()).append("', '");
                }
            }

            return s.toString();
        }

        private String formatLiterals() {
            if (expectedLiterals.length == 1) {
                return expectedLiterals[0];
            } else if (expectedLiterals.length == 2) {
                return expectedLiterals[0] + "' or '" + expectedLiterals[1];
            }

            var s = new StringBuilder();

            for (int i = 0; i < expectedLiterals.length; i++) {
                if (i == expectedLiterals.length - 1) {
                    s.append(expectedTypes[i]);
                } else if (i == expectedLiterals.length - 2 && expectedLiterals.length - 2 != 0) {
                    s.append(expectedLiterals[i]).append("', or '");
                } else {
                    s.append(expectedLiterals[i]).append("', '");
                }
            }

            return s.toString();
        }

        public void check(Token token) throws SalaException {
            if (!Arrays.stream(expectedTypes).toList().contains(token.getType())) {
                if (expectedTypes[0] == TokenTypes.NONE) throw new SalaException(token.err("expected EOL, but found '%s'", token.getType().name().toLowerCase()));
                if (token.is(TokenTypes.NONE)) throw new SalaException(token.err("expected '%s', but found EOL", formatTypes()));
                throw new SalaException(token.err("expected '%s', but found '%s'", formatTypes(), token.getType().name().toLowerCase()));
            } else if (expectedTypes[0] != TokenTypes.NONE && expectedLiterals.length != 0) {
                if (!Arrays.stream(expectedLiterals).toList().contains(token.getLiteral())) {
                    if (token.is(TokenTypes.NONE)) throw new SalaException(token.err("expected '%s', but found EOL", formatLiterals()));
                    throw new SalaException(token.err("expected '%s', but found '%s'", formatLiterals(), token.getLiteral()));
                }
            }
        }
    }

    private ExpectedToken[] expected;

    public Instruction() {
        this.expected = new ExpectedToken[0];
    }

    public Instruction(Token[][] tokens) {
        this.expected = new ExpectedToken[0];
    }

    public Instruction expectEOL() {
        return expect(TokenTypes.NONE);
    }

    public Instruction expectTrailing(TokenTypes[] types) {
        var n = new ExpectedToken[expected.length + 1];
        System.arraycopy(expected, 0, n, 0, expected.length);
        var nextT = new ExpectedToken(types, new String[0]);
        nextT.trailing = true;
        n[n.length - 1] = nextT;
        this.expected = n;
        return this;
    }

    public Instruction expectTrailing(TokenTypes type) {
        return expect(new TokenTypes[]{type});
    }

    public Instruction expect(TokenTypes[] types, String[] literal) {
        var n = new ExpectedToken[expected.length + 1];
        System.arraycopy(expected, 0, n, 0, expected.length);
        n[n.length - 1] = new ExpectedToken(types, literal);
        this.expected = n;
        return this;
    }

    public Instruction expect(TokenTypes type, String literal) {
        return expect(new TokenTypes[]{type}, new String[]{literal});
    }

    public Instruction expect(TokenTypes type, String[] literal) {
        return expect(new TokenTypes[]{type}, literal);
    }

    public Instruction expect(TokenTypes[] types) {
        return expect(types, new String[0]);
    }

    public Instruction expect(TokenTypes type) {
        return expect(new TokenTypes[]{type});
    }

    public void check(Token[] tl) throws SalaException {
        if (tl.length == 0) return;

        if (expected.length == 0) {
            expectEOL();
        } else if (expected[expected.length - 1].expectedTypes[0] != TokenTypes.NONE && !expected[expected.length - 1].trailing) {
            expectEOL();
        }

        for (int i = 0; i < expected.length; i++) {
            if (expected[i].trailing) {
                var e = expected[i];
                for (int j = i; j < tl.length; j++) {
                    if (!Arrays.stream(e.expectedTypes).toList().contains(tl[j].getType())) {
                        throw new SalaException(tl[j].err("expected '%s', but found '%s' instead", e.formatTypes(), tl[j].getType().name().toLowerCase()));
                    }
                }
                break;
            } else {
                expected[i].check(i < tl.length ? tl[i] : new Token());
            }
        }
    }
}
