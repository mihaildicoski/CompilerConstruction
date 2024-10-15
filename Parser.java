import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;



public class Parser {

    private String[][] parseTable; 
    private List<Token> tokens; 
    private Stack<Integer> stateStack; 
    private Stack<String> symbolStack; 
    private Map<String, Integer> tokenToColumnIndexMap;

    public Parser(List<Token> tokens, String parseTableFilePath) throws IOException{
        this.tokens = tokens; 
        this.stateStack = new Stack<>(); 
        this.stateStack.push(0); 
        // this.parseTable = parseTable; 
        this.symbolStack = new Stack<>(); 

        this.parseTable = readCSV(parseTableFilePath);
        this.tokenToColumnIndexMap = new HashMap<>(); 
        mapTokensToColumns();  
        //for debugging
        // System.out.println(Arrays.asList(tokenToColumnIndexMap));
        // System.out.println(tokenToColumnIndexMap.size());
        // System.out.println(Arrays.deepToString(parseTable));
        //should be 's33'
        // System.out.println(tokens.get(50).getValue().toString());
        // System.out.println("This is the action: " + getAction(11, tokens.get(50)));

        // System.out.println(Arrays.toString(parseTable[1]));
        // System.out.println(parseTable[9][7]);
        // System.out.println(getColumnIndex(tokens.get(3)));
        // System.out.println("that should've printed out 2");
        // System.out.println(tokens.get(3).getValue());
    }

    private String[] splitCSV(String line){
        List<String> res = new ArrayList<>(); 
        StringBuilder curr = new StringBuilder(); 
        boolean insideQuotes = false; 

        for(char c: line.toCharArray()){
            if(c == '"'){
                insideQuotes = !insideQuotes; 
            }
            else if(c == ',' && !insideQuotes){
                res.add(curr.toString().trim()); 
                curr.setLength(0);
            }
            else{
                curr.append(c); 
            }
        }
        res.add(curr.toString().trim()); 
        return res.toArray(new String[0]); 
    }

    private String[][] readCSV(String filePath) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(filePath));
        // StringBuilder sb = new StringBuilder();
        List<String[]> rows = new ArrayList<>();
        String line; 

        while ((line = br.readLine()) != null) {
            rows.add(splitCSV(line)); 
        }
       
        br.close();
        // System.out.println(rows.toArray(new String[0][0]));
        return rows.toArray(new String[0][0]); 

    
    }

    private void mapTokensToColumns() {
        String[] headers = parseTable[1]; 
        
        // System.out.println("Headers in CSV:");
        // for (String header : headers) {
        //     System.out.print("[" + header + "] ");  
        // }
        // System.out.println();


        for (int i = 0; i < headers.length; i++) {
            if (!headers[i].isEmpty()) {  
                tokenToColumnIndexMap.put(headers[i], i);
            }
        }
    }

    public void parse(){
        int tokenIndex = 0; 
        while (tokenIndex < tokens.size()) {   

            Token currToken = tokens.get(tokenIndex); 
            int currState = stateStack.peek(); 
            String action = getAction(currState, currToken); //lookup method for the parse table

            if(action.startsWith("s")){
                //shift
                int nextState = Integer.parseInt(action.substring(1)); 
                stateStack.push(nextState); 
                symbolStack.push(currToken.getType().toString());
                tokenIndex++; 
                // System.out.println("shift"); 
            }
            else if(action.startsWith("r")){
                //reduce
                int prodNum = Integer.parseInt(action.substring(1));
                applyReduce(prodNum); 
                // System.out.println("reduce"); 

            }
            else if(action.startsWith("acc")){
                //accept
                System.out.println("Reached accept state.");
                return; 
            }
            else if(isGoAction(action)){
                //go 
                int newState = Integer.parseInt(action); 
                stateStack.push(newState); 
                // System.out.println("go"); 

            }
            else{
                System.out.println("Syntax error. Curr token: "+ currToken);
                return; 
            }

        }
    }

    private boolean isGoAction(String action) {
       //csv file only has numbers before the go actions and no g so need to use regex
        return action.matches("\\d+");
    }

    private String getAction(int state, Token token){
        //this will have to looked over again to sort out the logic for finding values within the csv - will solve later
        //technically first column will always give the state number
        int columnindex = getColumnIndex(token); 
        if (columnindex == -1) {
            throw new IllegalArgumentException("Token was not found in parse table: " + token.getType());
        }
        //this is because the states only start from row 2 
        int adjustedState = state + 2; 
        int adjustedColIndex = columnindex; //because they start counting from 1
        // System.out.println("This is the state that they put: "+ adjustedState);
        // System.out.println("This is the colIndex that they put: "+ adjustedColIndex);
        System.out.println("This is the state stack: ");
        System.out.println(stateStack.toString());
        System.out.println("This is the symbol stack:");
        System.out.println(symbolStack.toString());
        System.out.println("This is the action that was found: " + parseTable[adjustedState][adjustedColIndex]);
        return parseTable[adjustedState][adjustedColIndex]; 
    }

    private int getColumnIndex(Token token){
        //this will return the column in csv with given token type
        //if token is of type keyword, operator, puncutation then you have to take the value
        Boolean useValue = false; 
        if(token.getType().toString().equals("KEYWORD")){
            useValue = true; 
        }
        else if(token.getType().toString().equals("OPERATOR")){
            useValue = true; 
        }
        else if(token.getType().toString().equals("PUNCTUATION")){
            useValue = true; 
        }
        
        if(useValue){
            return tokenToColumnIndexMap.getOrDefault(token.getValue(), -1); 
        }
        return tokenToColumnIndexMap.getOrDefault(token.getType().toString(), -1);
    }

    private void applyReduce(int productionNumber){
        //will apply reduce based on the production rule number given 
        //remember that goTo always follows a reduce 
        //check number of x = terminals/nonTerminals in rule then pop x off the stack
        //check what is on top of the stack atm then apply reduce given that production rules 'grouping'
        switch (productionNumber) {
            case 0:
                //S-> PROG
                symbolStack.pop(); 
                stateStack.pop(); 
                symbolStack.push("S");
                goTo("S");
                break;

            case 1:
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("PROG");
                goTo("PROG");
                break;

            case 2:
                
                symbolStack.push("GLOBVARS");
                goTo("GLOBVARS");
                break;

            case 3:
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("GLOBVARS");
                goTo("GLOBVARS");
                break;

            case 4:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("VTYP");
                goTo("VTYP");
                break;

            case 5:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("VTYP");
                goTo("VTYP");
                break;

            case 6:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("VNAME");
                goTo("VNAME");
                break;

            case 7:
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ALGO");
                goTo("ALGO");
                break;

            case 8:
                
                symbolStack.push("INSTRUC");
                goTo("INSTRUC");
                break;

            case 9:
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("INSTRUC");
                goTo("INSTRUC");
                break;

            case 10:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 11:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 12:
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 13:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 14:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 15:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 16:
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 17:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ATOMIC");
                goTo("ATOMIC");
                break;

            case 18:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ATOMIC");
                goTo("ATOMIC");
                break;

            case 19:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("CONST");
                goTo("CONST");
                break;

            case 20:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("CONST");
                goTo("CONST");
                break;

            case 21:
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ASSIGN");
                goTo("ASSIGN");
                break;

            case 22:
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ASSIGN");
                goTo("ASSIGN");
                break;

            case 23:
                for(int i  = 0; i<8; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("CALL");
                goTo("CALL");
                break;

            case 24:
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BRANCH");
                goTo("BRANCH");
                break;

            case 25:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 26:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 27:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 28:
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("OP");
                goTo("OP");
                break;

            case 29:
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("OP");
                goTo("OP");
                break;

            case 30:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ARG");
                goTo("ARG");
                break;

            case 31:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("ARG");
                goTo("ARG");
                break;

            case 32:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COND");
                goTo("COND");
                break;

            case 33:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COND");
                goTo("COND");
                break;

            case 34:
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("SIMPLE");
                goTo("SIMPLE");
                break;

            case 35:
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMPOSIT");
                goTo("COMPOSIT");
                break;

            case 36:
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("COMPOSIT");
                goTo("COMPOSIT");
                break;

            case 37:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("UNOP");
                goTo("UNOP");
                break;

            case 38:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("UNOP");
                goTo("UNOP");
                break;

            case 39:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 40:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 41:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 42:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 43:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 44:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 45:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 46:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 47:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("FNAME");
                goTo("FNAME");
                break;

            case 48:
                symbolStack.push("FUNCTIONS");
                goTo("FUNCTIONS");
                break;

            case 49:
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("FUNCTIONS");
                goTo("FUNCTIONS");
                break;

            case 50:
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("DECL");
                goTo("DECL");
                break;

            case 51:
                for(int i  = 0; i<9; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("HEADER");
                goTo("HEADER");
                break;

            case 52:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("FTYP");
                goTo("FTYP");
                break;

            case 53:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("FTYP");
                goTo("FTYP");
                break;

            case 54:
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("BODY");
                goTo("BODY");
                break;

            case 55:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("PROLOG");
                goTo("PROLOG");
                break;

            case 56:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("EPILOG");
                goTo("EPILOG");
                break;

            case 57:
                for(int i  = 0; i<9; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("LOCALVARS");
                goTo("LOCALVARS");
                break;

            case 58:
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                }
                symbolStack.push("SUBFUNCS");
                goTo("SUBFUNCS");
                break;



            
        
            default:
                System.out.println("Invalid production number(exceeds 58).");
                break;
        }
    }

    private void goTo(String nonTerminal){
        int newState = Integer.parseInt(getAction(stateStack.peek(), new Token(TokenType.KEYWORD, nonTerminal))); 
        stateStack.push(newState); 
    }

    
}




