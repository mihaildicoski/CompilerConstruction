import java.util.List; 

public class IntermediateTranslator {


    private Node rootNode; 
    private SymbolTable rootTable; 
    private StringBuilder miki = new StringBuilder(); 
    private int labelCounter = 0;
    protected int varCounter;  

    public IntermediateTranslator(Node root, SymbolTable rootTable){
        this.rootNode = root; 
        this.rootTable = rootTable; 
        this.varCounter = getMaxVar(rootTable);
    }

    public String translate(){
        traverse(rootNode);
        return miki.toString();  
    }

    public int getMaxVar(SymbolTable rootTable){
        SymbolTable clone = rootTable; 
        int maxVar = 0; 
        
        for (SymbolInfo info : clone.table.values()) {
            if (info.uniqueName.startsWith("v")) {
                try {
                    int varNum = Integer.parseInt(info.uniqueName.substring(1));
                    if (varNum > maxVar) {
                        maxVar = varNum;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        for (SymbolTable child : clone.getChildren()) {
            int result = getMaxVar(child); 
            
                try {
                    int varNum = result;
                    if (varNum > maxVar) {
                        maxVar = varNum;
                    }
                } catch (NumberFormatException e) {
                }
            
        }
    
    return (maxVar); 
    }


    public String newVar(){
        return "v" + (++varCounter); 
    }

    private void traverse(Node node){
        String type = node.getType(); 
        switch (node.getType()) {
            case "PROG":
                Node mainNode = node.getChildren().get(0);
                Node globvars = node.getChildren().get(1); 
                Node algo = node.getChildren().get(2);
                Node functions = node.getChildren().get(3);
                //traverse(mainNode);
                //traverse(globvars);
                traverse(algo); 
                miki.append(" STOP "); 
                traverse(functions);
                break;

            case "ALGO":
                Node instruc = node.getChildren().get(1);  
                traverse(instruc);
                break; 
            
            case "INSTRUC":
                if(node.getChildren().size()>0){

                    traverse(node.getChildren().get(0)); 
                    traverse(node.getChildren().get(2));
                }  
                else{
                    miki.append(" REM END "); 
                }
                break; 

            case "COMMAND": 
                String commandvalue = node.getChildren().get(0).getValue();
                String commandType = node.getChildren().get(0).getType(); 
                if(commandType.equals("KEYWORD")){
                    switch (commandvalue) {
                        case "skip":
                            miki.append(" REM DO NOTHING "); 
                            break;
    
                        case "halt": 
                            miki.append(" STOP ");
                            break; 
                            
                        case "print": 
                            miki.append(" PRINT ");
                            traverse(node.getChildren().get(1));
                            break;  
    
                        case "return": 
                            //phase 5b
                            break; 
                    
                        default:
                            break;
                    }
                }
                else{
                    traverse(node.getChildren().get(0));
                }
                

                break; 

            

            case "ATOMIC": 
                String childType = node.getChildren().get(0).getType(); 
                switch (childType) {
                    case "VNAME":
                        //we can assume we are only ever going to be in main scope for now until we translate functions 
                        //will modify tableRoot upon encounter with functions node as we know we will be entering diff scope
                        String valueVname = node.getChildren().get(0).getChildren().get(0).getValue();
                        String uniqueNameForVname = ""; 
                        SymbolTable rootTableClone = rootTable;  //don't want to modify rootTable position whilst traversing up the tree 
                        if(returnEntry(valueVname, rootTable) != null){
                            uniqueNameForVname = returnEntry(valueVname, rootTable).uniqueName; 
                        }
                        else{
                            SymbolInfo infoResult = null; 
                            while (returnEntry(valueVname, rootTableClone) == null && rootTableClone.getParent()!=null) {
                                rootTableClone = rootTableClone.getParent(); 
                                infoResult = returnEntry(valueVname, rootTableClone); 
                            }
                            if(infoResult != null){
                                uniqueNameForVname = infoResult.uniqueName; 
                            }
                            else{
                                //theoretically should never be thrown as scope analysis handles for this
                               throw new IllegalArgumentException("Internal name for "+ valueVname + " not found in symbol tables.");  
                            }
                        }
                        miki.append(uniqueNameForVname); 
                        break;

                    case "CONST": 
                        if(node.getChildren().get(0).getChildren().get(0).getType().equals("NUMERIC_LITERAL")){
                            miki.append(node.getChildren().get(0).getChildren().get(0).getValue()); 
                        }
                        else if(node.getChildren().get(0).getChildren().get(0).getType().equals("TEXT_LITERAL")){
                            miki.append('"'+node.getChildren().get(0).getChildren().get(0).getValue()+'"'); 
                        }
                        break; 
                
                    default:
                        break;
                }
                break;


            case "ASSIGN":
                Node secondChild = node.getChildren().get(1); 
                if(secondChild.getValue().equals("=")){
                    traverse(node.getChildren().get(0));
                    miki.append(":=");
                    traverse(node.getChildren().get(2));   
                }
                else{
                    miki.append("INPUT ");
                    traverse(node.getChildren().get(2)); 
                }
                break; 


            case "CALL":
                miki.append("CALL_"); 
                String funcKey = node.getChildren().get(0).getChildren().get(0).getValue(); 
                String newFuncName = returnEntry(funcKey, rootTable).uniqueName;
                miki.append(newFuncName+"("); 
                traverse(node.getChildren().get(2));
                miki.append(",");
                traverse(node.getChildren().get(4));
                miki.append(",");
                traverse(node.getChildren().get(6));
                miki.append(")"); 
                break; 


            case "OP": 
                String place = newVar(); 
                break; 
                
                
            
            
                
            
                
                
           

             
                

        
            default:
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                
        }
    }


    public SymbolTable findSymbolTable(String tableName, SymbolTable root){

        SymbolTable temp = root; 
        if(temp.getName().equals(tableName)){
            return temp; 
        }
        else{
            for(SymbolTable child: temp.getChildren()){
                SymbolTable res = findSymbolTable(tableName, child); 
                if(res!=null){
                    return res; 
                }
            }
            return null; 
        }

    }



    public SymbolInfo returnEntry(int id, SymbolTable rootTable) {
       
        for (SymbolInfo info : rootTable.table.values()) {
            if (info.nodeId == id) {
                return info; 
            }
        }
        
        for (SymbolTable child : rootTable.getChildren()) {
            SymbolInfo result = returnEntry(id, child);
            if (result != null) {
                return result; 
            }
        }
    
        
        return null;
    }





    public SymbolInfo returnEntry(String key, SymbolTable rootTable) {
        SymbolInfo info = rootTable.lookup(key);
        if (info != null) {
            return info; 
        }
        
        for (SymbolTable child : rootTable.getChildren()) {
            SymbolInfo result = returnEntry(key, child);
            if (result != null) {
                return result;  
            }
        }
    
        return null;
    }

    


    
        

    

    


    
}
