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
        switch (node.getType()) {
            case "PROG":
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                break;

            case "FUNCTION_DECL": 
                translateFunc(node);
                break; 
                
            case "ASSIGN": 
                translateAss(node);
                break;  

            case "RETURN":
                translateRet(node);
                break;  

             
                

        
            default:
                for(Node child: node.getChildren()){
                    traverse(child);
                }
                
        }
    }


    private void translateFunc(Node node){
        String funcName = getFunctionName(node); 
        String args = getArgs(node.getArgs());
        miki.append("function ").append(funcName).append("(").append(args).append("){\n");  
        for(Node body: node.getBody().getChildren()){
            traverse(body); 
        }
        miki.append("}\n"); 
    }

    private void translateAss(Node node){
        String id = getLHS(node).getValue(); 
        String rhs = translateAtom(getRHS(node));
        miki.append(id).append(" := ").append(rhs).append("\n");  
    }

    private void translateRet(Node node){
        String retvalue = node.getReturnValue().getValue(); 
        miki.append("RETURN ").append(retvalue).append("\n"); 
    }

    private String translateAtom(Node node){
        if(node.getType().equals("VARIABLE_IDENTIFIER")){
            return node.getValue(); 
        }
        else if(node.getType().equals("NUMERIC_LITERAL")){
            return node.getValue(); 
        }
        return ""; 

    }

    private String getArguments(List<Node> argNodes){
        String temp = ""; 
        for(Node argNode: argNodes){
            temp += argNode.getValue() + ","; 
        }
        if(temp.length()>0){
            temp = temp.substring(0, temp.length()-1); 
        }
        return temp; 
    }

    private Node getLHS(Node parent){
        return parent.getChildren().get(0); 
    }

    private Node getRHS(Node parent){
        return parent.getChildren().get(1); 
    }

    private String getFunctionName(Node node){
        if(node.getType().equals("FUNCTION_IDENTIFIER")){
            return node.getValue(); 
        }
        return ""; 
    }   


    
}
