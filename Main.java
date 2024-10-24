import java.util.List;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;

public class Main {
    public static void main(String[] args) {
        try {
            String code = ""; 
            String filepath = "input4.txt"; 
            StringBuilder sb = new StringBuilder(); 

            try (BufferedReader reader = new BufferedReader(new FileReader(filepath))) {
                String line; 
                while ((line = reader.readLine()) != null) {
                    sb.append(line).append(" ");  
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
            writeToXML(tokens, "lexed.txt"); 

            Parser parser = new Parser(tokens, "slrparse2.csv"); 
            Node root = parser.parse();
            ASTConverter astToXml = new ASTConverter();
            String xmlOutput = astToXml.generateSyntaxTreeXml(root);
            try (FileWriter writer = new FileWriter("AST.txt")) {
                writer.write(xmlOutput);
            } catch (IOException e) {
                e.printStackTrace();
            }

            ScopeAnalyser2 scope = new ScopeAnalyser2(root); 
            SymbolTable tableRoot = scope.analyse(root);
            scope.printTables(tableRoot); 
            // String xmlOutput2 = astToXml.generateSyntaxTreeXml(updatedRoot);
            // try (FileWriter writer = new FileWriter("updatedAST.txt")) {
            //     writer.write(xmlOutput2);
            // } catch (IOException e) {
            //     e.printStackTrace();
            // }

             
            System.out.println("=============================Typechecker=============================");
            TypeChecker typeChecker = new TypeChecker(root, tableRoot);
            boolean checkPass =typeChecker.checkTypes();

            if(checkPass){
                System.out.println("Type checking passed");
            }else{
                //System.out.println("Type checking failed");
                throw new IllegalArgumentException("Type checker returned false");
            }



            //IR
            //tree names need to get renamed directly (make another tree)
            IntermediateTranslator ir = new IntermediateTranslator(root, tableRoot); 
            String intermediateCode = ir.translate(); 
            System.out.println("==========================INTERMEDIATE CODE==========================");
            System.out.println(intermediateCode);


        } catch (IOException e) {
            System.out.println("Error reading the input file: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("There was an error: " + e.getMessage());
        }
    }



    public static void writeToXML(List<Token> tokens, String filename){

        try (FileWriter writer = new FileWriter(filename)) {
            writer.write("<TOKENSTREAM>\n");
            for (Token token : tokens) {
                writer.write("  <TOK>\n");
                writer.write("    <ID>" + token.getId() + "</ID>\n");
                writer.write("    <CLASS>" + token.getType() + "</CLASS>\n");
                writer.write("    <WORD>" + token.getValue() + "</WORD>\n");
                writer.write("  </TOK>\n");
            }
            writer.write("</TOKENSTREAM>\n");
            System.out.println("Lexed tokens were successfully written to " + filename);
        } catch (IOException e) {
            System.out.println("Error writing the lexed tokens to file: " + e.getMessage());
        }

    }


}
