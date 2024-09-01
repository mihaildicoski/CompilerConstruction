import java.util.List;

public class Main {
    public static void main(String[] args) {
        

        String code = "main num V_x, text V_y, num V_z begin if eq(V_x, 10) then print V_x else print V_y end";
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.lex();

        System.out.println("The string input was: ");
        System.out.println(code);
        for (Token token : tokens) {
            System.out.println(token);
        }





    }
}