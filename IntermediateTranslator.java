import java.util.List; 

public class IntermediateTranslator {


    private Node rootNode; 
    private StringBuilder miki = new StringBuilder(); 
    private int labelCounter = 0; 

    public IntermediateTranslator(Node root){
        this.rootNode = root; 
    }

    public String translate(){
        traverse(rootNode);
        return miki.toString();  
    }

    private void traverse(Node node){
        String type = node.getType(); 
        switch (node.getType()) {
            case "PROG":
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                break;
            
            case "ASSIGN": 
                translateAssignment(node); 
                break; 

            case "INSTRUC":
                translateInstruction(node);
                break; 

            case "GLOBVARS":
                translateGlobvars(node);
                break; 
                
            
                
                
           

             
                

        
            default:
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                
        }
    }


    
        
    private void translateAssignment(Node node){

        String lhs = node.getChildren().get(0).getChildren().get(0).getValue(); //will give the variable identifier value (v0 for example)
        String rhs = node.getChildren().get(2).getChildren().get(0).getChildren().get(0).getChildren().get(0).getValue(); //will return the numeric/text literal value 

        String alles = lhs + " := " + rhs + "\n"; 
        miki.append(alles); 

    }

    private void translateInstruction(Node node){
        //command can either have assign or {keyword, atomic} as children 
        //base case for when instruc is epsilon 
        if(node.getChildren()==null || node.getChildren().size() == 0){
            return; 
        }
        Node comm = node.getChildren().get(0); 
        if(comm.getChildren().size()>0 && comm.getChildren().get(0).getType().equals("ASSIGN")){
            translateAssignment(comm.getChildren().get(0));
        }
        else if(comm.getChildren().size()>0 && comm.getChildren().get(0).getType().equals("KEYWORD")){

            Node keywordnode = comm.getChildren().get(0); 
            switch (keywordnode.getValue()) {
                case "print":
                    Node atomNode = comm.getChildren().get(1); 
                    String atomValue = atomNode.getChildren().get(0).getChildren().get(0).getValue(); 
                    miki.append("PRINT ").append(atomValue).append("\n"); 
                    break;

                case "halt":
                    miki.append("HALT\n"); 
                    break;  
            
                default:
                    break;
            }

        }   

        //; is always going to be after COMMAND so it its getChildren().get(1)
        Node secondInstruc = node.getChildren().get(2); 
        translateInstruction(secondInstruc);
            
        
    }

    private void translateGlobvars(Node node){

        //GLOBVARS -> VTYP VNAME , GLOBVARS 
        //Node vtyp = node.getChildren().get(0); 
        //Node lhs = node.getChildren().get(1); 
        

    }
        

    

    


    
}
