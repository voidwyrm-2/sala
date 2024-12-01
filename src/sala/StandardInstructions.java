package sala;

import java.util.HashMap;
import java.util.Map;

public class StandardInstructions {
    public static Map<String, SalaEntry> get(SalaStack stack, Object[] vars, SalaOutput out, Map<String, Integer> labels) {
        var _1 = new HashMap<>((Map.of(
                "print", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (stack.empty()) throw new SalaException(it.err("stack underflow"));
                    out.print(stack.pop());
                }),
                "push", new SalaEntry(new Instruction().expect(new TokenTypes[]{TokenTypes.STRING, TokenTypes.NUMBER}), (_, _, args) -> {
                    var a = args[0];
                    if (a.is(TokenTypes.STRING)) {
                        stack.push(a.getLiteral());
                    } else if (a.is(TokenTypes.NUMBER)) {
                        stack.push(Integer.parseInt(a.getLiteral()));
                    }
                }),
                "clear", new SalaEntry(new Instruction().expect(TokenTypes.NONE), (_, _, _) -> stack.clear()),
                "dup", new SalaEntry(new Instruction().expect(TokenTypes.NONE), (_, it, _) -> {
                    if (stack.empty()) throw new SalaException(it.err("stack underflow"));
                    var v = stack.pop();
                    stack.push(v);
                    stack.push(v);
                }),
                "add", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    switch (a) {
                        case Integer i when b instanceof Integer -> stack.push(i + (Integer) b);
                        case Float f when b instanceof Float -> stack.push(f + (Float) b);
                        case Integer i when b instanceof Float -> stack.push(i + (Float) b);
                        case Float f when b instanceof Integer -> stack.push(f + (Integer) b);
                        case String s when b instanceof String -> stack.push(s + b);
                        case null, default ->
                                throw new SalaException(it.err("invalid types for addition: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    }
                }),
                "sub", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    switch (a) {
                        case Integer i when b instanceof Integer -> stack.push(i - (Integer) b);
                        case Float f when b instanceof Float -> stack.push(f - (Float) b);
                        case Integer i when b instanceof Float -> stack.push(i - (Float) b);
                        case Float f when b instanceof Integer -> stack.push(f - (Integer) b);
                        case null, default ->
                                throw new SalaException(it.err("invalid types for subtraction: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    }
                }),
                "mul", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    switch (a) {
                        case Integer i when b instanceof Integer -> stack.push(i * (Integer) b);
                        case Float f when b instanceof Float -> stack.push(f * (Float) b);
                        case Integer i when b instanceof Float -> stack.push(i * (Float) b);
                        case Float f when b instanceof Integer -> stack.push(f * (Integer) b);
                        case String s when b instanceof Integer -> {
                            stack.push(s.repeat((Integer) b));
                        }
                        case null, default ->
                                throw new SalaException(it.err("invalid types for multiplication: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    }
                }),
                "div", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    switch (a) {
                        case Integer i when b instanceof Integer -> stack.push(i / (Integer) b);
                        case Float f when b instanceof Float -> stack.push(f / (Float) b);
                        case Integer i when b instanceof Float -> stack.push(i / (Float) b);
                        case Float f when b instanceof Integer -> stack.push(f / (Integer) b);
                        case null, default ->
                                throw new SalaException(it.err("invalid types for division: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    }
                }),
                "mod", new SalaEntry(new Instruction(), (_, it, _) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    switch (a) {
                        case Integer i when b instanceof Integer -> stack.push(i % (Integer) b);
                        case Float f when b instanceof Float -> stack.push(f % (Float) b);
                        case Integer i when b instanceof Float -> stack.push(i % (Float) b);
                        case Float f when b instanceof Integer -> stack.push(f % (Integer) b);
                        case null, default ->
                                throw new SalaException(it.err("invalid types for modulus: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    }
                }),
                "j", new SalaEntry(new Instruction().expect(TokenTypes.WORD), (index, it, args) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    var jumpPos = labels.get(args[0].getLiteral());
                    if (jumpPos == null)
                        throw new SalaException(args[0].err("label '%s' does not exist", args[0].getLiteral()));
                    index.value = jumpPos;
                })
        )));

        var _2 = new HashMap<>(Map.of(
                "jeq", new SalaEntry(new Instruction().expect(TokenTypes.WORD), (index, it, args) -> {
                    // System.out.println("reached jeq");
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    var jumpPos = labels.get(args[0].getLiteral());
                    if (jumpPos == null) throw new SalaException(args[0].err("label '%s' does not exist", args[0].getLiteral()));
                    // System.out.println(a.equals(b) ? "a equals b" : "a does not equal b");
                    if (a.equals(b)) {
                        index.value = jumpPos;
                    }
                }),
                "jne", new SalaEntry(new Instruction().expect(TokenTypes.WORD), (index, it, args) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    var jumpPos = labels.get(args[0].getLiteral());
                    if (jumpPos == null) throw new SalaException(args[0].err("label '%s' does not exist", args[0].getLiteral()));
                    if (!a.equals(b)) {
                        index.value = jumpPos;
                    }
                }),
                "jlt", new SalaEntry(new Instruction().expect(TokenTypes.WORD), (index, it, args) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    var jumpPos = labels.get(args[0].getLiteral());
                    if (jumpPos == null) throw new SalaException(args[0].err("label '%s' does not exist", args[0].getLiteral()));

                    boolean jump =  switch (a) {
                        case Integer i when b instanceof Integer -> i < (Integer) b;
                        case Float f when b instanceof Float -> f < (Float) b;
                        case Integer i when b instanceof Float -> i < (Float) b;
                        case Float f when b instanceof Integer -> f < (Integer) b;
                        case null, default ->
                                throw new SalaException(it.err("invalid types for less than: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    };

                    //System.out.println(jump ? String.format("%s is less than %s", a, b) : String.format("%s is not less than %s", a, b));

                    if (jump) {
                        index.value = jumpPos;
                    }
                }),
                "jgt", new SalaEntry(new Instruction().expect(TokenTypes.WORD), (index, it, args) -> {
                    if (!stack.hasAmount(2)) throw new SalaException(it.err("stack underflow"));
                    var b = stack.pop();
                    var a = stack.pop();
                    if (a == null || b == null) throw new NullPointerException(it.err("a or b is null"));

                    var jumpPos = labels.get(args[0].getLiteral());
                    if (jumpPos == null) throw new SalaException(args[0].err("label '%s' does not exist", args[0].getLiteral()));

                    boolean jump =  switch (a) {
                        case Integer i when b instanceof Integer -> i > (Integer) b;
                        case Float f when b instanceof Float -> f > (Float) b;
                        case Integer i when b instanceof Float -> i > (Float) b;
                        case Float f when b instanceof Integer -> f > (Integer) b;
                        case null, default ->
                                throw new SalaException(it.err("invalid types for less than: '%s' and '%s'", a.getClass().toString().toLowerCase(), b.getClass().toString().toLowerCase()));
                    };

                    if (jump) {
                        index.value = jumpPos;
                    }
                })
        ));

        /*
        var inception_1 = new HashMap<>(Map.of(
                "", new SalaEntry();
        ));
        */

        var outMap = new HashMap<String, SalaEntry>();
        outMap.putAll(_1);
        outMap.putAll(_2);

        //outMap.putAll(inception_1);

        return outMap;
    }
}
