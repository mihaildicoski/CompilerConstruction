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

    public SymbolTable analyse(Node root){
        scopeStack.enterScope();
        scopeStack.currentScope().setName("main"); 
        traverse(root); 
        SymbolTable rootTable = scopeStack.currentScope(); 
        scopeStack.exitScope();   
        // System.out.println("Func and var internal renaming......");
        // printAST(root, " ");
        // System.out.println(scopeStack.printSymbolTables());
        return rootTable; 
    }

    public void printTables(SymbolTable rootSymbolTable){
        System.out.println(rootSymbolTable.toString());
        for(SymbolTable child: rootSymbolTable.getChildren()){
            printTables(child); 
        }
    }

   

    private void traverse(Node node){
        String type = node.getType(); 
        switch(node.getType()){
            case "PROG": 
                //change this to order main GLOBVARS FUNCTIONS ALGO (last two swap)
                // for(Node child: node.getChildren()){
                //     traverse(child);
                // }
                Node mainNode = node.getChildren().get(0);
                Node globvars = node.getChildren().get(1); 
                Node algo = node.getChildren().get(2);
                Node functions = node.getChildren().get(3);
                traverse(mainNode);
                traverse(globvars);
                traverse(functions);
                traverse(algo);  
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
                        //maybe traverse header before you enter a new scope
                        //traverse(header);
                        //make sure you have to 
                        scopeStack.enterScope();
                        scopeStack.currentScope().setName(functionName);
                        //scope is entered on the decl
                        for(Node child: node.getChildren()){
                            traverse(child);
                        }
                        scopeStack.exitScope();
                    }
                    
                }
                break; 

            case "HEADER": 
                //possibly collect all vnames and then enter scope then add 
                //you will exit out of the scope onc children are traversed
                String functionName = node.getChildren().get(1).getChildren().get(0).getValue();
                String vname1 = node.getChildren().get(3).getChildren().get(0).getValue();
                int id1 = node.getChildren().get(3).getChildren().get(0).getId();
                String vname2 = node.getChildren().get(5).getChildren().get(0).getValue();
                int id2 = node.getChildren().get(5).getChildren().get(0).getId(); 
                String vname3 = node.getChildren().get(7).getChildren().get(0).getValue();
                int id3 = node.getChildren().get(7).getChildren().get(0).getId(); 
                String newName1 = "v"+(vCounter++);
                String newName2 = "v"+(vCounter++);
                String newName3 = "v"+(vCounter++);
                // SymbolInfo existing = scopeStack.currentScope().lookup(vname1); 
                // SymbolInfo existing2 = scopeStack.currentScope().lookup(vname2); 
                // SymbolInfo existing3 = scopeStack.currentScope().lookup(vname3); 
                scopeStack.currentScope().addSymbol(vname1, newName1, "num", id1);
                scopeStack.currentScope().addSymbol(vname2, newName2, "num", id2);
                scopeStack.currentScope().addSymbol(vname3, newName3, "num", id3);


                //this is for when you call a function - check the incoming params in the symbol table
                // if(existing!=null && existing2 !=null && existing3 !=null){
                //     //the passed in params exist in the scope and this is legal 
                //     String name1 = existing.uniqueName; 
                //     String name2 = existing2.uniqueName; 
                //     String name3 = existing3.uniqueName; 
                //     //scopeStack.enterScope();

                //     scopeStack.currentScope().addSymbol(vname1, name1, "num", id1);
                //     scopeStack.currentScope().addSymbol(vname2, name2, "num", id2);
                //     scopeStack.currentScope().addSymbol(vname3, name3, "num", id3);
                // }
                // else{
                //     //the incoming params are not delcared in the parent scope 
                //     System.out.println("Incoming parameters for function " + functionName + " are not declared beforehand. ");
                //     return; 
                // }

               

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
                List<Node> childrenfrom3 = node.getChildren().subList(2, 5);
                for(Node child: childrenfrom3){
                    traverse(child);
                }

                break; 

            case "VNAME": 
                //make sure to add to symbol table 
                String origName = node.getChildren().get(0).getValue(); 
                SymbolInfo existingvname = scopeStack.currentScope().lookup(origName);
                if(existingvname != null){
                    node.getChildren().get(0).setValue(existingvname.uniqueName);
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
                    SymbolInfo existingvar = scopeStack.currentScope().lookup(originalName);
                    if(existingvar!=null){
                        //declared beforehand throw an error
                        System.out.println("Globvar with name "+ originalName+ " already declared.");
                        return; 
                    }
                    else{
                        String uniqueName = "v" + (vCounter++); 
                        int id = vname.getChildren().get(0).getId(); 
                        scopeStack.currentScope().addSymbol(originalName, uniqueName, typeVar, id);
                        vname.getChildren().get(0).setValue(uniqueName);
                        traverse(node.getChildren().get(3));
                    }
                }
                break; 

            case "CALL": 
                Node fname = node.getChildren().get(0);
                String funcName = fname.getChildren().get(0).getValue();
                SymbolInfo exists = scopeStack.currentScope().lookup(funcName); 
                if(exists != null){
                    //function call is valid
                    //check that all vars that are passed in are withing curr scope then if valid then you can create a new scope
                    Node atom1 = node.getChildren().get(2);
                    Node atom2 = node.getChildren().get(4);
                    Node atom3 = node.getChildren().get(6);
                    String atomType1 = atom1.getChildren().get(0).getType(); 
                    String atomType2 = atom2.getChildren().get(0).getType(); 
                    String atomType3 = atom3.getChildren().get(0).getType(); 
                    if(atomType1.equals("VNAME")){
                        String atomName1 = atom1.getChildren().get(0).getChildren().get(0).getValue();
                        SymbolInfo existing = scopeStack.currentScope().lookup(atomName1); 
                        if(existing!=null){
                            //valid passed in param because exists already 
                        }
                        else{
                            //while getParent() is not null keep checking parent symbol table to see if it exists
                            //if not then throw error if exists then you can continue
                            SymbolTable parent = scopeStack.currentScope().getParent(); 
                            boolean found = false; 
                            while(parent!=null){
                                SymbolInfo temporaryexists = parent.lookup(atomName1); 
                                if(temporaryexists!=null){
                                    //we found the var in an upper scope we can break
                                    found = true; 
                                    break; 
                                }
                                parent = parent.getParent(); 
                            }
                            if(!found){
                                System.out.println("Parameter "+atomName1+" that was passed into function was invalid.");
                                return; 
                            }
                        }
                        
                    }

                    if(atomType2.equals("VNAME")){
                        String atomName2 = atom2.getChildren().get(0).getChildren().get(0).getValue();
                        SymbolInfo existing = scopeStack.currentScope().lookup(atomName2); 
                        if(existing!=null){
                            //valid passed in param because exists already 
                        }
                        else{
                            
                            SymbolTable parent = scopeStack.currentScope().getParent(); 
                            boolean found = false; 
                            while(parent!=null){
                                SymbolInfo temporaryexists = parent.lookup(atomName2); 
                                if(temporaryexists!=null){
                                    //we found the var in an upper scope we can break
                                    found = true; 
                                    break; 
                                }
                                parent = parent.getParent(); 
                            }
                            if(!found){
                                System.out.println("Parameter "+atomName2+" that was passed into function was invalid.");
                                return; 
                            }
                        }
                    }

                    if(atomType3.equals("VNAME")){
                        String atomName3 = atom3.getChildren().get(0).getChildren().get(0).getValue();
                        SymbolInfo existing = scopeStack.currentScope().lookup(atomName3); 
                        if(existing!=null){
                            //valid passed in param because exists already 
                        }
                        else{
                            SymbolTable parent = scopeStack.currentScope().getParent(); 
                            boolean found = false; 
                            while(parent!=null){
                                SymbolInfo temporaryexists = parent.lookup(atomName3); 
                                if(temporaryexists!=null){
                                    //we found the var in an upper scope we can break
                                    found = true; 
                                    break; 
                                }
                                parent = parent.getParent(); 
                            }
                            if(!found){
                                System.out.println("Parameter "+atomName3+" that was passed into function was invalid.");
                                return; 
                            }
                        }
                    }
                    
                    //at this point passed in params were either consts or they all passed (they were in the scope)
                    System.out.println("All of the passed in parameter were in the scope or upper scope.");



                }
                else{
                    //function was not declared
                    System.out.println("Function "+ fname+" was not declared. Invalid call. ");

                }
                    

             
            case "ATOMIC": 
                String atomType = node.getChildren().get(0).getType();
                if(atomType.equals("VNAME")){
                    //check if the var was declared previously 
                    String atomName = node.getChildren().get(0).getChildren().get(0).getValue();
                    SymbolInfo existing = scopeStack.currentScope().lookup(atomName); 
                    if(existing!=null){
                        //valid passed in param because exists already 
                    }
                    else{
                        //while getParent() is not null keep checking parent symbol table to see if it exists
                        //if not then throw error if exists then you can continue
                        SymbolTable parent = scopeStack.currentScope().getParent(); 
                        boolean found = false; 
                        while(parent!=null){
                            SymbolInfo temporaryexists = parent.lookup(atomName); 
                            if(temporaryexists!=null){
                                //we found the var in an upper scope we can break
                                found = true; 
                                break; 
                            }
                            parent = parent.getParent(); 
                        }
                        if(!found){
                            System.out.println("Variable "+atomName+" is not declared.");
                            return; 
                        }
                    }

                }

                break; 


            case "ASSIGN": 
                //check that the vname was declared beforehand 
                Node vname = node.getChildren().get(0); 
                String actualVname = vname.getChildren().get(0).getValue(); 
                SymbolInfo existing = scopeStack.currentScope().lookup(actualVname); 
                if(existing != null){
                    
                }
                else{
                    SymbolTable parent = scopeStack.currentScope().getParent(); 
                    boolean found = false; 
                    while(parent!=null){
                        SymbolInfo temporaryexists = parent.lookup(actualVname); 
                        if(temporaryexists!=null){
                            found = true; 
                            break; 
                        }
                        parent = parent.getParent(); 
                    }
                    if(!found){
                        System.out.println("Variable "+actualVname+" is not declared.");
                        return; 
                    }
                }
                break; 





            default: 
                for(Node child: node.getChildren()){
                    traverse(child); 
                }
                    

        }
    }



    // private void traverseCalls(Node node){

    //     String type = node.getType(); 
    //     switch(node.getType()){
    //         case "CALL": 
                


    //         default: 
    //             for(Node child: node.getChildren()){
    //                 traverseCalls(child);
    //             }
    //     }

    // }



}









class ScopeStack{
    private Stack<SymbolTable> scopeStack = new Stack<>(); 
    private List<SymbolTable> symbolTables = new ArrayList<SymbolTable>();


    public void enterScope() {
        if(!scopeStack.isEmpty()){
            SymbolTable parent = scopeStack.peek(); 
            scopeStack.push(new SymbolTable()); 
            scopeStack.peek().setParent(parent);
        }
        else{
            scopeStack.push(new SymbolTable()); 
        }
    }
    public void exitScope() {
        //print out the symbol Table

        // System.out.println("This is working.................");
        SymbolTable temp = scopeStack.peek();
        // System.out.println(temp.toString());  
        symbolTables.add(temp); 
        scopeStack.pop(); 
        if(!scopeStack.isEmpty()){
            scopeStack.peek().addChild(temp);
        }
    }
    public SymbolTable currentScope() {
        return scopeStack.peek();
    }

    public String printSymbolTables(){
        String temp = ""; 
        for(SymbolTable st : symbolTables){
            temp += st.toString();
            temp += "\n";  
        }
        return temp; 

    }
}

class SymbolTable{
    private Map<String, SymbolInfo> table = new HashMap<>();
    private String tableName; 
    private List<SymbolTable> children = new ArrayList<>();
    private SymbolTable parent; 

    public void addSymbol(String originalName, String uniqueName, String type, int nodeId) {
        table.put(originalName, new SymbolInfo(uniqueName, type, nodeId));
    }

    public SymbolInfo lookup(String name) {
        return table.get(name);  
    }

    @Override
    public String toString() {
        StringBuilder temp = new StringBuilder("Symbol Table: " + tableName+ "\n");
        for (Map.Entry<String, SymbolInfo> entry : table.entrySet()) {
            String originalName = entry.getKey();
            SymbolInfo info = entry.getValue();
            temp.append("Original Name: ").append(originalName)
                .append(", Unique Name: ").append(info.uniqueName)
                .append(", Type: ").append(info.type)
                .append(", Node ID: ").append(info.nodeId)
                .append("\n");  
        }
        return temp.toString();
    }

    public void setName(String name){
        this.tableName = name; 
    }

    public void addChild(SymbolTable n){
        n.setParent(this); 
        this.children.add(n);
    }

    public void setParent(SymbolTable parent){
        this.parent = parent; 
    }

    public SymbolTable getParent(){
        return this.parent; 
    }

    public List<SymbolTable> getChildren(){
        return this.children; 
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