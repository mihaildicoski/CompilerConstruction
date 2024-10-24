import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
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
    private Stack<Node> astStack = new Stack<>(); 
    private int nodeId = 0;  

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

    public Node parse(){
        int tokenIndex = 0; 
        
        while (tokenIndex < tokens.size()) {   

            Token currToken = tokens.get(tokenIndex); 
            int currState = stateStack.peek(); 
            String action = getAction(currState, currToken); //lookup method for the parse table

            if(action.startsWith("s")){
                //shift
                int nextState = Integer.parseInt(action.substring(1)); 
                stateStack.push(nextState); 
                //this can also be getType() for higher level representation
                symbolStack.push(currToken.getType().toString());
                Node tempy = new Node(currToken.getType().toString(), currToken.getValue().toString(), nodeId++); 
                astStack.push(tempy); 
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
                Node root = astStack.peek(); 
                printAST(root, "");
                return root; 
            }
            else if(isGoAction(action)){
                //go 
                int newState = Integer.parseInt(action); 
                stateStack.push(newState); 
                // System.out.println("go"); 

            }
            else{
                System.out.println("Syntax error. Curr token: "+ currToken);
                return null; 
            }

        }
        return null; 
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
            throw new IllegalArgumentException("Token was not found in parse table: " + token.getType() + " value: "+ token.getValue());
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
        System.out.println("This is the AST stack: ");
        System.out.println(astStack.toString());
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
        else if(token.getType().toString().equals("EOF")){
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
        Node parentNode; 
        List<Node> children = new ArrayList<>();
        switch (productionNumber) {
            case 0:
                //S-> PROG
                symbolStack.pop();
                parentNode = new Node("S", nodeId++);
                parentNode.addChild(astStack.pop());  
                //Node childNode = new Node("childnode", symbolStack.pop()); 
                //Node parentNode = new Node("S"); 
                //parentNode.addChild(childNode);
                //astStack.push(parentNode); 
                stateStack.pop(); 
                symbolStack.push("S");
                astStack.push(parentNode); 
                goTo("S");
                break;

            case 1:
                parentNode = new Node("PROG", nodeId++); 
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode); 
                symbolStack.push("PROG");
                goTo("PROG");
                break;

            case 2:
                parentNode = new Node("GLOBVARS", nodeId++); 
                astStack.push(parentNode); 
                symbolStack.push("GLOBVARS");
                goTo("GLOBVARS");
                break;

            case 3:
                parentNode = new Node("GLOBVARS", nodeId++); 
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("GLOBVARS");
                goTo("GLOBVARS");
                break;

            case 4:
                parentNode = new Node("VTYP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("VTYP");
                goTo("VTYP");
                break;

            case 5:
                parentNode = new Node("VTYP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("VTYP");
                goTo("VTYP");
                break;

            case 6:
                parentNode = new Node("VNAME", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("VNAME");
                goTo("VNAME");
                break;

            case 7:
                parentNode = new Node("ALGO", nodeId++); 
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ALGO");
                goTo("ALGO");
                break;

            case 8:
                parentNode = new Node("INSTRUC", nodeId++); 
                astStack.push(parentNode); 
                symbolStack.push("INSTRUC");
                goTo("INSTRUC");
                break;

            case 9:
                parentNode = new Node("INSTRUC", nodeId++); 
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("INSTRUC");
                goTo("INSTRUC");
                break;

            case 10:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 11:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 12:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 13:
                parentNode = new Node("COMMAND", nodeId++);  
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 14:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 15:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 16:
                parentNode = new Node("COMMAND", nodeId++); 
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMMAND");
                goTo("COMMAND");
                break;

            case 17:
                parentNode = new Node("ATOMIC", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ATOMIC");
                goTo("ATOMIC");
                break;

            case 18:
                parentNode = new Node("ATOMIC", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ATOMIC");
                goTo("ATOMIC");
                break;

            case 19:
                parentNode = new Node("CONST", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("CONST");
                goTo("CONST");
                break;

            case 20:
                parentNode = new Node("CONST", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("CONST");
                goTo("CONST");
                break;

            case 21:
                parentNode = new Node("ASSIGN", nodeId++); 
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop());
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ASSIGN");
                goTo("ASSIGN");
                break;

            case 22:
                parentNode = new Node("ASSIGN", nodeId++); 
                for(int i  = 0; i<3; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ASSIGN");
                goTo("ASSIGN");
                break;

            case 23:
                parentNode = new Node("CALL", nodeId++); 
                for(int i  = 0; i<8; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("CALL");
                goTo("CALL");
                break;

            case 24:
                parentNode = new Node("BRANCH", nodeId++); 
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BRANCH");
                goTo("BRANCH");
                break;

            case 25:
                parentNode = new Node("TERM", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 26:
                parentNode = new Node("TERM", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 27:
                parentNode = new Node("TERM", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("TERM");
                goTo("TERM");
                break;

            case 28:
                parentNode = new Node("OP", nodeId++); 
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("OP");
                goTo("OP");
                break;

            case 29:
                parentNode = new Node("OP", nodeId++); 
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("OP");
                goTo("OP");
                break;

            case 30:
                parentNode = new Node("ARG", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ARG");
                goTo("ARG");
                break;

            case 31:
                parentNode = new Node("ARG", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("ARG");
                goTo("ARG");
                break;

            case 32:
                parentNode = new Node("COND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COND");
                goTo("COND");
                break;

            case 33:
                parentNode = new Node("COND", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COND");
                goTo("COND");
                break;

            case 34:
                parentNode = new Node("SIMPLE", nodeId++); 
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("SIMPLE");
                goTo("SIMPLE");
                break;

            case 35:
                parentNode = new Node("COMPOSIT", nodeId++); 
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMPOSIT");
                goTo("COMPOSIT");
                break;

            case 36:
                parentNode = new Node("COMPOSIT", nodeId++); 
                for(int i  = 0; i<4; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("COMPOSIT");
                goTo("COMPOSIT");
                break;

            case 37:
                parentNode = new Node("UNOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("UNOP");
                goTo("UNOP");
                break;

            case 38:
                parentNode = new Node("UNOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("UNOP");
                goTo("UNOP");
                break;

            case 39:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 40:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 41:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 42:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 43:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 44:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 45:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 46:
                parentNode = new Node("BINOP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BINOP");
                goTo("BINOP");
                break;

            case 47:
                parentNode = new Node("FNAME", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("FNAME");
                goTo("FNAME");
                break;

            case 48:
                parentNode = new Node("FUNCTIONS", nodeId++); 
                astStack.push(parentNode); 
                symbolStack.push("FUNCTIONS");
                goTo("FUNCTIONS");
                break;

            case 49:
                parentNode = new Node("FUNCTIONS", nodeId++); 
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("FUNCTIONS");
                goTo("FUNCTIONS");
                break;

            case 50:
                parentNode = new Node("DECL", nodeId++); 
                for(int i  = 0; i<2; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("DECL");
                goTo("DECL");
                break;

            case 51:
                parentNode = new Node("HEADER", nodeId++); 
                for(int i  = 0; i<9; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("HEADER");
                goTo("HEADER");
                break;

            case 52:
                parentNode = new Node("FTYP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("FTYP");
                goTo("FTYP");
                break;

            case 53:
                parentNode = new Node("FTYP", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("FTYP");
                goTo("FTYP");
                break;

            case 54:
                parentNode = new Node("BODY", nodeId++); 
                for(int i  = 0; i<6; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("BODY");
                goTo("BODY");
                break;

            case 55:
                parentNode = new Node("PROLOG", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop();
                    children.add(astStack.pop());  
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("PROLOG");
                goTo("PROLOG");
                break;

            case 56:
                parentNode = new Node("EPILOG", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("EPILOG");
                goTo("EPILOG");
                break;

            case 57:
                parentNode = new Node("LOCVARS", nodeId++); 
                for(int i  = 0; i<9; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
                symbolStack.push("LOCVARS");
                goTo("LOCVARS");
                break;

            case 58:
                parentNode = new Node("SUBFUNCS", nodeId++); 
                for(int i  = 0; i<1; i++){
                    symbolStack.pop(); 
                    stateStack.pop(); 
                    children.add(astStack.pop()); 
                }
                Collections.reverse(children); 
                for(Node child: children){
                    parentNode.addChild(child);  
                }
                astStack.push(parentNode);
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

    public void printAST(Node node, String indent) {
        if (node == null) return;
        System.out.println(indent + node.getType() + (node.getValue() != null ? ": " + node.getValue() : ""));
        for (Node child : node.getChildren()) {
            printAST(child, indent + "  ");
        }
    }

    
}




