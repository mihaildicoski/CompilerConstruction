public enum TokenType{
    //specific keywords
    MAIN, NUM, TEXT, BEGIN, END, SKIP, HALT, PRINT,
    INPUT, IF, THEN, ELSE, VOID, NOT, SQRT, OR, AND, EQ, 
    GRT, ADD, SUB, MUL, DIV, 

    //general categories
    KEYWORD, //check value
    VARIABLE_IDENTIFIER, //keep 
    FUNCTION_IDENTIFIER, //keep
    NUMERIC_LITERAL, //keep 
    TEXT_LITERAL, //keep
    OPERATOR, //check value
    PUNCTUATION, //check value

    //keyword, operator, punctuation


    

 }