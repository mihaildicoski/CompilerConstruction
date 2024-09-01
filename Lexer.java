
import java.util.*; 
import java.util.List; 
import java.util.regex.*; 



public class Lexer {

    private String input; 
    private int currPosition; 

    public Lexer(String input){
        this.input = input; 
        this.currPosition = 0; 
    }

    public List<Token> lex(){
        List<Token> tokens = new ArrayList<>(); 
        while(currPosition<input.length()){

            Token token = nextToken(); 

            
            if (token!=null) {
                tokens.add(token); 
            }
            else{
                currPosition++; 
            }

        }
        return tokens; 
    }



    private Token nextToken(){
        if (currPosition>=input.length()) {
            return null; 
        }

        while(currPosition<input.length() && Character.isWhitespace(input.charAt(currPosition))){
            currPosition++; 
        }



        String[] tokenPatterns = {
            //this can all be found in the recSPL document
            "V_[a-z]([a-z]|[0-9])*", // Identifiers (Variable names)
            "F_[a-z]([a-z]|[0-9])*", // Identifiers (Function names)
            "[0-9]+(\\.[0-9]+)?|-[0-9]+(\\.[0-9]+)?", // Numeric literals (Token-Class N)
            "\"[A-Z][a-z]{0,7}\"", // Text literals (Token-Class T)
            "\\+|\\-|\\*|\\/|=|<|>|!", // Operators
            "[.,;(){}]", // Punctuation
            "main|num|text|begin|end|skip|halt|print|input|if|then|else|void|not|sqrt|or|and|eq|grt|add|sub|mul|div", // Keywords
        }; 

        TokenType[] tokenTypes = {
            TokenType.VARIABLE_IDENTIFIER,
            TokenType.FUNCTION_IDENTIFIER,
            TokenType.NUMERIC_LITERAL,
            TokenType.TEXT_LITERAL,
            TokenType.OPERATOR,
            TokenType.PUNCTUATION,
            TokenType.KEYWORD, 
        }; 

        for (int i = 0; i < tokenPatterns.length; i++) {
            Pattern pattern = Pattern.compile("^" + tokenPatterns[i]);
            Matcher matcher = pattern.matcher(input.substring(currPosition)); 
            
            if(matcher.find()){
                String value = matcher.group(); 
                currPosition += value.length(); 
                return new Token(tokenTypes[i], value); 
            }
        }
        currPosition++; 
        return null; 

    }

    
}