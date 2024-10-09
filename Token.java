

public class Token {

    private TokenType type; 
    private String value; 
    private int id; 
    private static int idCounter = 0; 

    public Token(TokenType type, String value){
        this.id = ++idCounter; 
        this.type = type; 
        this.value = value; 
    }

    public int getId(){
        return id; 
    }

    public TokenType getType(){
        return type; 
    }

    public String getValue(){
        return value; 
    }

    @Override
    public String toString(){
        return "Token("+"Type:"+type+", Value:'"+value+"')"; 
    }
    
    
}
