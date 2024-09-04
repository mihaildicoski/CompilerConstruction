import java.util.List;
import java.io.FileReader;
import java.io.IOException;
import java.io.BufferedReader;

public class Main {
    public static void main(String[] args) {
        try {
            String code = ""; 
            String filepath = "input.txt"; 
            StringBuilder sb = new StringBuilder(); 

            try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
                String line; 
                while ((line = reader.readLine()) != null) {
                    sb.append(line);  
                }
                code = sb.toString(); 
            } 

           
            Lexer lexer = new Lexer(code);
            List<Token> tokens = lexer.lex();

            System.out.println("The string input was: ");
            System.out.println(code);
            for (Token token : tokens) {
                System.out.println(token);
            }

        } catch (IOException e) {
            System.out.println("Error reading the input file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("There was an error: " + e.getMessage());
        }
    }
}
