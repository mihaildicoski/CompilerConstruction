import java.util.List;

public class Main {
    public static void main(String[] args) {
        

        // String code = "main num V_x, text V_y, num V_z begin if eq(V_x, 10) then print V_x else print V_y end";
        //found a bug that lexer does not work as soon as i put a < symbol in the input code
        String code = "main num V_x, text V_y, num V_z begin V_x < input if eq(V_x, 10) then print V_x else print \"Value\" end F_addthree(V_x, V_y, V_z) end void F_addthree(num V_a, num V_b, num V_c) begin V_c = add(V_a, 3) print V_c end"; 
        Lexer lexer = new Lexer(code);
        List<Token> tokens = lexer.lex();

        System.out.println("The string input was: ");
        System.out.println(code);
        for (Token token : tokens) {
            System.out.println(token);
        }







    }
}