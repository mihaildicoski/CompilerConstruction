import java.util.*; 

public class ScopeAnalyser2 {
    

    private Node rootNode; 
    private int vCounter = 0; 
    private int fCounter = 0; 
    private ScopeStack scopeStack = new ScopeStack(); 

    public ScopeAnalyser2(Node root){
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
        return root; 
    }

    private void traverse(Node node){
        String type = node.getType(); 
        switch(node.getType()){
            case "PROG": 
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                break; 

            case "FUNCTIONS":
                //first check that no other function has same name
                if(node.getChildren().size()>0){
                    Node decl = node.getChildren().get(0); 
                    Node header = decl.getChildren().get(0); 
                    Node fname = header.getChildren().get(1);
                    String functionType = header.getChildren().get(0).getChildren().get(0).getValue(); 
                    String functionName = fname.getChildren().get(0).getValue(); 
                    int functionId = fname.getChildren().get(0).getId(); 
                    SymbolInfo existing = scopeStack.currentScope().lookup(functionName); 
                    if(existing != null){
                        //then we have a function in the same scope with same name throw error
                        System.out.println("There is already a function declared with name: " + functionName+ " within the same scope.");
                        return; 
                    }
                    else{
                        String newName = "f" + (fCounter++); 
                        scopeStack.currentScope().addSymbol(functionName, newName, functionType, functionId);
                        scopeStack.enterScope();
                        for(Node child: node.getChildren()){
                            traverse(child);
                        }
                        scopeStack.exitScope();
                    }
                    
                }
                break; 

            case "HEADER": 
                String vname1 = node.getChildren().get(3).getChildren().get(0).getValue();
                int id1 = node.getChildren().get(3).getChildren().get(0).getId();
                String vname2 = node.getChildren().get(5).getChildren().get(0).getValue();
                int id2 = node.getChildren().get(5).getChildren().get(0).getId(); 
                String vname3 = node.getChildren().get(7).getChildren().get(0).getValue();
                int id3 = node.getChildren().get(7).getChildren().get(0).getId(); 
                String newName1 = "v"+(vCounter++);
                String newName2 = "v"+(vCounter++);
                String newName3 = "v"+(vCounter++);

                scopeStack.currentScope().addSymbol(vname1, newName1, "num", id1);
                scopeStack.currentScope().addSymbol(vname2, newName2, "num", id2);
                scopeStack.currentScope().addSymbol(vname3, newName3, "num", id3);

                break; 
            
            case "BODY": 
                Node localvars = node.getChildren().get(1); 

                String type1 = localvars.getChildren().get(0).getChildren().get(0).getValue();//0 3 6 
                String type2 = localvars.getChildren().get(3).getChildren().get(0).getValue();
                String type3 = localvars.getChildren().get(6).getChildren().get(0).getValue(); 

                String prvi = localvars.getChildren().get(1).getChildren().get(0).getValue(); 
                String drugi = localvars.getChildren().get(4).getChildren().get(0).getValue(); 
                String treci = localvars.getChildren().get(7).getChildren().get(0).getValue(); 

                int prvi_id = localvars.getChildren().get(1).getChildren().get(0).getId(); 
                int drugi_id = localvars.getChildren().get(4).getChildren().get(0).getId(); 
                int treci_id = localvars.getChildren().get(7).getChildren().get(0).getId();

                String noviName1 = "v"+(vCounter++);
                String noviName2 = "v"+(vCounter++);
                String noviName3 = "v"+(vCounter++);

                scopeStack.currentScope().addSymbol(prvi, noviName1, type1, prvi_id);
                scopeStack.currentScope().addSymbol(drugi, noviName2, type2, drugi_id);
                scopeStack.currentScope().addSymbol(treci, noviName3, type3, treci_id);

                //finished localvars 

                break; 

            case "VNAME": 
                //make sure to add to symbol table 
                String origName = node.getChildren().get(0).getValue(); 
                SymbolInfo existing = scopeStack.currentScope().lookup(origName);
                if(existing != null){
                    node.getChildren().get(0).setValue(existing.uniqueName);
                } 
                else{
                    //only exit scope if you are not in global 
                   scopeStack.exitScope(); 
                   if(scopeStack.currentScope()!=null){

                       SymbolInfo existingParent = scopeStack.currentScope().lookup(origName); 
                       if(existingParent!=null){
                         node.getChildren().get(0).setValue(existingParent.uniqueName);
                       }

                   }
                   else{
                    //you were in global scope and you could not find vname inside the symbol table
                   }
                }
                
                break; 


            case "BRANCH": 
                //make sure to have scope inside both algo parts
                break; 

            case "GLOBVARS": 
                if(node.getChildren().size()>0){
                    Node vtyp = node.getChildren().get(0); 
                    Node vname = node.getChildren().get(1); 
                    String typeVar = vtyp.getChildren().get(0).getValue(); 
                    String originalName = vname.getChildren().get(0).getValue(); 
                    String uniqueName = "v" + (vCounter++); 
                    int id = vname.getChildren().get(0).getId(); 
                    scopeStack.currentScope().addSymbol(originalName, uniqueName, typeVar, id);
                    vname.getChildren().get(0).setValue(uniqueName);
                    traverse(node.getChildren().get(3));
                }
                break; 





            default: 
                for(Node child: node.getChildren()){
                    traverse(child); 
                }
                    

        }
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