import java.util.List;
import java.util.Stack;

public class Parser {

    private List<Token> tokens; 
    private int currTokenIndex = 0; 
    private Stack<Integer> stateStack = new Stack<>(); 
    private Stack<Integer> symbolStack = new Stack<>(); 

    public Parser(List<Token> tokens){
        this.tokens = tokens; 
        this.stateStack.push(0); //this is the start
    }

    
}
