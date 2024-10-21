import java.util.*; 

public class ScopeAnalyser {

    private Node rootNode; 
    private int vCounter = 0; 
    private int fCounter = 0;  
    private ScopeStack scopeStack = new ScopeStack(); 


    public ScopeAnalyser(Node root){
        this.rootNode = root; 
        printAST(rootNode, " ");
    }

    public void printAST(Node node, String indent) {
        if (node == null) return;
        System.out.println(indent + "ID: "+ node.getId() + " " + node.getType() + (node.getValue() != null ? ": " + node.getValue() : ""));
        for (Node child : node.getChildren()) {
            printAST(child, indent + "  ");
        }
    }

    public Node analyse(Node root){
        scopeStack.enterScope(); 
        traverse(root); 
        scopeStack.exitScope();  
        System.out.println("Func and var internal renaming......");
        printAST(root, " ");
        // System.out.println(scopeStack.toString());
        return root; 
    }

    private void traverse(Node node) {
        //without the scope management (scopeStack) you will have 
        //scenarios whereby var x in inner scope of a code block 
        //will shadow var x in global scope. In order to mitigate this 
        //we have to create scopeStack where each scopeStack has a symbol table.
        //If everyone just shared the same symbol table then inner scope declarations would 
        //take precedence and overwrite all global scope vars - subsequently causing errors in the program. 
        if (isVarDecl(node)) {
            String originalName = node.getValue();  
            SymbolInfo existing = scopeStack.currentScope().lookup(originalName); 
            if(existing == null){
                String uniqueName = "v" + (vCounter++);
                scopeStack.currentScope().addSymbol(originalName, uniqueName, "var", node.getId());
                node.setValue(uniqueName);  //set to vx
            }
            else{
                //this just sets to already existing internal name 
                node.setValue(existing.uniqueName);
            }

        }
        else if(isVarUsage(node)){
            String originalName = node.getValue(); 
            SymbolInfo existing = scopeStack.currentScope().lookup(originalName);  
            if(existing!=null){
                node.setValue(existing.uniqueName);
            }
            else{
                //this gets entered when you have localvars with vtyp vname 
                //does the usage of vars within a func need new vars or can we check prev scope? 
                //check global scope and see if it was declared there - if not throw error
            }
        }
        else if (isFuncDecl(node)) {
            // String originalName = node.getValue();  
            // String uniqueName = "f" + (fCounter++);
            // //obviously checks curr scope then adds - if same var name in same scope then it will just overwrite 
            // scopeStack.currentScope().addSymbol(originalName, uniqueName, "function", node.getId());
            // node.setValue(uniqueName);  // set to fx
            // scopeStack.enterScope();  
            // traverse(getFunctionBody(node));
            // scopeStack.exitScope();

            Node decl = node.getChildren().get(0);
            Node header = decl.getChildren().get(0);  
            String functionType = header.getChildren().get(0).getValue(); //can be num or void
            String originalName = header.getChildren().get(1).getChildren().get(0).getValue();
            SymbolInfo existing = scopeStack.currentScope().lookup(originalName); 
            if(existing != null){
                //throw error because you have a function in the same scope with the same name
                System.out.println("Function with same name was declared twice...");
                return; 
            }
            else{
                String uniqueName = "f" + (fCounter++);
                //this can be changed to the actual function_identifier node id - but it is set 
                //as the FUNCTIONS id for now 
                scopeStack.currentScope().addSymbol(originalName, uniqueName, functionType, node.getId());
                header.getChildren().get(1).getChildren().get(0).setValue(uniqueName);
                scopeStack.enterScope();
                traverse(getFunctionBody(node.getChildren().get(0))); 
                scopeStack.exitScope();
            }
            
            //recursive call
            traverse(node.getChildren().get(1)); 

        } else if (isBlock(node)) {
            scopeStack.enterScope();  
            for (Node child : node.getChildren()) {
                traverse(child);
            }
            scopeStack.exitScope();  

        } 
        else if(isGlobvars(node)){
            //GLOBVARS -> VTYP VNAME , GLOBVARS
            //GLOBVARS -> ''
            //VTYP -> num
            //VTYP -> text
            String type = node.getChildren().get(0).getChildren().get(0).getValue(); 
            String originalName = node.getChildren().get(1).getChildren().get(0).getValue(); 
            SymbolInfo existing = scopeStack.currentScope().lookup(originalName); 
            if(existing == null){
                String uniqueName = "v" + (vCounter++);
                scopeStack.currentScope().addSymbol(originalName, uniqueName, type, node.getId());
                node.getChildren().get(1).getChildren().get(0).setValue(uniqueName);  
            }
            else{
                //this just sets to already existing internal name 
                node.getChildren().get(1).getChildren().get(0).setValue(existing.uniqueName);
            }

            traverse(node.getChildren().get(3)); 

        }
        else {
            for (Node child : node.getChildren()) {
                traverse(child);
            }
        }
    }

    public Boolean isVarDecl(Node node){
        
        if(node.getType().equals("VARIABLE_IDENTIFIER")){
            Node parent = node.getParent();
            if(parent!=null){
                return parent.getType().equals("VNAME") && parent.getParent().getType().equals("ASSIGN"); 
            }
        }
        return false; 
    }
    
    public Boolean isFuncDecl(Node node){
        if(node.getType().equals("FUNCTIONS")){
            if(node.getChildren().size()>0){
                return true; 
            }
        }
        return false; 
    }

    public Boolean isBlock(Node node){
        return node.getType().equals("ALGO") || node.getType().equals("BODY") || node.getType().equals("BRANCH");
    }

    public Node getFunctionBody(Node node) {
        for (Node child : node.getChildren()) {
            if (child.getType().equals("BODY")) {
                return child;
            }
        }
        return null;  
    }


    public Boolean isVarUsage(Node node){
      
        if(node.getType().equals("VARIABLE_IDENTIFIER")){
            Node parent = node.getParent(); 
            if(parent != null){
                return !parent.getType().equals("ASSIGN"); 
            }
        }
        return false; 

    }

    public Boolean isGlobvars(Node node){
        if(node.getType().equals("GLOBVARS")){
            if(node.getChildren().size()>0){ //if epsilon we don't want it to run the case statement
                return true; 
            }
        }
        return false; 
    }


    
}


class ScopeStack{
    private Stack<SymbolTable> scopeStack = new Stack<>(); 

    public void enterScope() {
        scopeStack.push(new SymbolTable()); 
    }
    public void exitScope() {
        scopeStack.pop();  
    }
    public SymbolTable currentScope() {
        return scopeStack.peek();
    }
}

class SymbolTable{
    private Map<String, SymbolInfo> table = new HashMap<>();

    public void addSymbol(String originalName, String uniqueName, String type, int nodeId) {
        table.put(originalName, new SymbolInfo(uniqueName, type, nodeId));
    }

    public SymbolInfo lookup(String name) {
        return table.get(name);  
    }
}

class SymbolInfo {
    String uniqueName;
    String type;
    int nodeId;

    public SymbolInfo(String uniqueName, String type, int nodeId) {
        this.uniqueName = uniqueName;
        this.type = type;
        this.nodeId = nodeId;
    }
}