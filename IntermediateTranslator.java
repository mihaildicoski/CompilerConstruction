import java.util.List;
import java.util.Stack; 

public class IntermediateTranslator {


    private Node rootNode; 
    private SymbolTable rootTable; 
    private StringBuilder miki = new StringBuilder(); 
    private int labelCounter = 0;
    protected int varCounter;  
    private SymbolTable currTable; 
    private Stack<String> placeStack = new Stack<>(); 

    public IntermediateTranslator(Node root, SymbolTable rootTable){
        this.rootNode = root; 
        this.rootTable = rootTable; 
        this.varCounter = getMaxVar(rootTable);
        this.currTable = rootTable; 
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

    public String newLabel(){
        return "l" + (labelCounter++); 
    }

    public String translateOp(Node binop){
        //op can either be unop or binop so BINOP -> keyword: value
        String originalValue = binop.getChildren().get(0).getValue(); 
        switch (originalValue) {
            case "eq":
                return "="; 
                

            case "grt":
                return ">";
                 

            case "add": 
                return "+"; 
                
            case "sub":
                return "-"; 

            case "mul": 
                return "*"; 

            case "div": 
                return "/"; 
        
            default:
                return ""; 
        }
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
                //traverse(functions);
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
                    Node termNode = node.getChildren().get(2);
                    String place = newVar(); 
                    placeStack.push(place); 
                    Node vnameNode = node.getChildren().get(0); 
                    String vNameValue = vnameNode.getChildren().get(0).getValue();
                    String x = returnEntry(vNameValue, currTable).uniqueName;
                    traverse(termNode); 
                    miki.append("["+x+":="+place+"] "); //dont pop off placeStack because you still need it down the traversal 
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


            //case "OP": 
            //     //place?
                // Node firstChild = node.getChildren().get(0);
                // if(firstChild.getType().equals("UNOP")){
                //     String place1 = newVar(); 
                //     traverse(node.getChildren().get(2)); //code1
                //     //miki.append(place+":="); //++place
                //     traverse(node.getChildren().get(0)); //opname
                //     miki.append("("+place1+")");
                // }
                // else if(firstChild.getType().equals("BINOP")){

                // }
                

            //     break; 


            case "OP":
                Node firstChild = node.getChildren().get(0); 
                if(firstChild.getType().equals("UNOP")){
                    String place1 = newVar(); 
                    traverse(node.getChildren().get(2)); //code1
                    //String op = translateOp(firstChild); 
                    miki.append("["+placeStack.pop()+":="); 
                    traverse(firstChild); 
                    miki.append("(" + place1 + ")"); 

                }
                else if(firstChild.getType().equals("BINOP")){
                    //exp1 binop 2 
                    String place1 = newVar(); 
                    String place2 = newVar(); 
                    placeStack.push(place1); 
                    traverse(node.getChildren().get(2));
                    placeStack.push(place2);
                    traverse(node.getChildren().get(4));
                    miki.append("["+placeStack.pop()+":="+place1+" ");
                    traverse(firstChild);
                    miki.append(place2);     
                }
                    
                break; 


            case "UNOP": 
                String unopValue = node.getChildren().get(0).getValue(); 
                if(unopValue.equals("not")){
                    //special case for not
                    //handled in cond
                }
                else if(unopValue.equals("sqrt")){
                    // String returnVal = translateOp(node); 
                    // miki.append(returnVal); 
                    miki.append(" SQR "); 
                    //traverse(node.getChildren().get()); 
                } 
                break; 

            case "BINOP": 


                String binopvalue = node.getChildren().get(0).getValue();
                if(binopvalue.equals("or")){
                    //cond1 || cond2 figure 6.8
                    

                }
                else if(binopvalue.equals("and")){
                    //case for and 
                }
                else{
                    String returnVal = translateOp(node); 
                    miki.append(returnVal); 
                }

                
                break;

            case "TERM": 
                if(node.getChildren().get(0).getType().equals("ATOMIC")){
                    placeStack.pop(); 
                }
                traverse(node.getChildren().get(0));
                break; 


            case "BRANCH":
                //if composit then 6.8
                //if simple then 6.5 with if COND then stat1 else stat2
                Node cond = node.getChildren().get(1);
                String condValue = cond.getChildren().get(0).getType(); 
                if(condValue.equals("COMPOSIT")){
                    //traverse();
                } 
                else if(condValue.equals("SIMPLE")){
                    String label1 = newLabel(); 
                    String label2 = newLabel(); 
                    String label3 = newLabel(); 
                    traverse(cond);
                    miki.append(" [LABEL "+label1+"]"); 
                    traverse(node.getChildren().get(3));
                    miki.append(" [GOTO "+label3+", LABEL "+label2+"]");  
                    traverse(node.getChildren().get(5));
                    miki.append(" [LABEL "+label3+ "]"); 

                }
                break; 

            
                
                
            
            case "VNAME":
                
                String valueVname = node.getChildren().get(0).getValue();
                String uniqueNameForVname = ""; 
                SymbolTable rootTableClone = rootTable;  
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
                       throw new IllegalArgumentException("Internal name for "+ valueVname + " not found in symbol tables.");  
                    }
                }
                miki.append(uniqueNameForVname); 
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
