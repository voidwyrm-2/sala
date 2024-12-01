import sala.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

class Main {
    private static String readline() {
        var s = new Scanner(System.in);
        var str = s.nextLine();
        s.close();
        return str;
    }

    private static void testTrailing() {
        try {
            var t = new Token[]{new Token(TokenTypes.WORD, ""), new Token(TokenTypes.NUMBER, ""), new Token(TokenTypes.NUMBER, "")};

            var i = new Instruction().expect(TokenTypes.WORD).expectTrailing(TokenTypes.NUMBER);
            i.check(t);
        } catch (SalaException e) {
            System.out.println(e.getMessage());
        }
        System.exit(0);
    }

    public static void main(String[] args) {
        // testTrailing();

        if (args.length < 1)  {
            System.out.println("expected sala [file]");
            System.exit(1);
        }

        String path = args[0];

        String content = "";
        try (Scanner s = new Scanner(new File(path))) {
            StringBuilder builder = new StringBuilder();
            while (s.hasNextLine()) builder.append(s.nextLine()).append("\n");
            content = builder.toString().trim();
        } catch (IOException e) {
            if (e.getClass().equals(FileNotFoundException.class)) {
                System.out.printf("file '%s' does not exist\n", path);
            } else {
                System.out.printf("unable to read file '%s'\n", path);
            }
            System.exit(1);
        }

        try {
            var interpreter = new Interpreter(Main::readline);
            interpreter.interpret(content);
        } catch (SalaException e) {
           System.out.println(e.getMessage());
           System.exit(1);
        }
    }
}