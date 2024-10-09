import java.util.List;
import java.util.Stack;

public class Parser {

    private String[][] parseTable; 
    private List<Token> tokens; 
    private Stack<Integer> stateStack; 
    private Stack<String> symbolStack; 

    public Parser(List<Token> tokens, String[][] parseTable){
        this.tokens = tokens; 
        this.stateStack = new Stack<>(); 
        this.stateStack.push(0); 
        this.parseTable = parseTable; 
        this.symbolStack = new Stack<>(); 
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
            }
            else if(action.startsWith("r")){
                //reduce
                int prodNum = Integer.parseInt(action.substring(1));
                applyReduce(prodNum);  
            }
            else if(action.startsWith("acc")){
                //accept
            }
            else if(isGoAction(action)){
                //go 
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
        int columnindex = getTokenColumnIndex(token); 
        return parseTable[state][columnindex]; 
    }

    private int getColumnIndex(Token token){
        //this will return the column in csv with given token type
        return 0; 
    }

    private void applyReduce(int productionNumber){
        //will apply reduce based on the production rule number given 
        //remember that goTo always follows a reduce 
        //check number of x = terminals/nonTerminals in rule then pop x off the stack
        //check what is on top of the stack atm then apply reduce given that production rules 'grouping'
    }

    
}
