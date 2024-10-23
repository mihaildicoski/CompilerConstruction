
//add imports here 

//this class will be the typechecker for the language given to us.
//language found in the grammar.txt file

//import java.lang.reflect.Type;          //dont need this yet
import java.util.List;

public class TypeChecker {

    private Node startNode;
    private SymbolTable symbolTable;

    TypeChecker(Node startNode, SymbolTable symbolTable) {
        this.startNode = startNode;
        this.symbolTable = symbolTable;
    }

    boolean checkTypes() {

        Node currentNode = startNode;

        // return checkTypes(currentNode);
        return true;
    }

    boolean checkTypes(Node currentNode) {

        // testing start node and children VALUES
        switch (currentNode.getType()) {
            case "PROG":

                boolean[] check = new boolean[currentNode.getChildren().size()];
                System.out.println("checking type PROG");
                int typeCount = 0;
                for (Node child : currentNode.getChildren()) {
                    check[typeCount] = checkTypes(child);
                    typeCount++;

                }
                for (boolean b : check) {
                    if (!b) {
                        return false;
                    }
                }
                return true;
            case "GLOBVARS":
                if (currentNode.getChildren().size() == 0) {
                    // GLOBVAR -> ""
                    return true;
                } else {
                    // GLOBVAR -> "VTYP VNAME , GLOBVARS"
                    List<Node> children = currentNode.getChildren();
                    String vtypValue = typeOfVTYP(children.get(0));

                    int id = children.get(1).getId();
                    // link (vtypValue, id) to symbol table

                    return checkTypes(children.get(3));
                }
            case "VNAME":
                // some other shit has to happen here maybe
                return true;

            case "ALGO":
                List<Node> children = currentNode.getChildren();
                return checkTypes(children.get(1));

            case "INSTRUC":
                if (currentNode.getChildren().size() == 0) {
                    // INSTRUC -> ''
                    return true;
                } else {
                    // INSTRUC -> COMMAND ; INSTRUC2
                    // List<Node> children = currentNode.getChildren();
                    boolean typeCommand = checkTypes(currentNode.getChildren().get(0));
                    boolean typeINSTRUC = checkTypes(currentNode.getChildren().get(2));

                    return (typeCommand && typeINSTRUC);
                }

            case "COMMAND":
                List<Node> childrenCommand = currentNode.getChildren();
                if (childrenCommand.get(0).getValue().equals("halt")) {
                    return true;
                } else if (childrenCommand.get(0).getValue().equals("skip")) {
                    return true;
                } else {
                    // COMMAND -> print ATOMIC
                    if (currentNode.getChildren().get(0).getValue().equals("print")) {

                        String typeAtom = typeOfATOMIC(currentNode.getChildren().get(1));
                        if (typeAtom.equals("n")) {
                            return true;
                        } else if (typeAtom.equals("t")) {
                            return true;
                        } else {
                            return false;
                        }
                    }
                    //COMMAND -> ASSIGN
                    if(currentNode.getChildren().get(0).getType().equals("ASSIGN")){
                        return checkTypes(currentNode.getChildren().get(0));
                    }else if(currentNode.getChildren().get(0).getType().equals("BRANCH")){
                        return checkTypes(currentNode.getChildren().get(0));
                    }
                    // add stuff for case COMMAND -> return ATOMIC
                    //COMMMAND -> return ATOMIC
                    if(currentNode.getChildren().get(0).getValue().equals("return")){
                        String typeAtom = typeOfATOMIC(currentNode.getChildren().get(1));

                        if(typeAtom.equals("n")){
                            return true;
                        }else{
                            return false;
                        }
                    }
                }

            case "ATOMIC":
                return true;

            case "ASSIGN":
                return true;
                

            default:
                return true;
        }
        // return true;
    }

    String typeOfVTYP(Node node) {

        switch (node.getChildren().get(0).getValue()) {
            case "num":
                return "n";
            case "text":
                return "t";
        }
        return "";
    }

    String typeOfATOMIC(Node node) {
        switch (node.getType()) {
            case "VNAME":
                // get the type of the VNAME from the symbol table
                return "n";
            case "CONST":
                return  typeOfCONST(node);

        }
        return "";
    }

    String typeOfCONST(Node node) {
        List<Node> children = node.getChildren();

        if (children.get(0).getType().equals("NUMERIC_LITERAL")) {
            return "n";

        } else if (children.get(0).getType().equals("TEXT_LITERAL")) {
            return "t";
        }
        return "";
    }


    // create a method for each non-terminal in grammar.txt
    //

    // func for PROG
    // get the children

    // case 1:
    // case 2:
}
