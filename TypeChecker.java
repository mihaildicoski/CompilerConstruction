
import java.util.List;

public class TypeChecker {

    private Node startNode;
    private SymbolTable symbolTable;
    String currentFunctionName;

    TypeChecker(Node startNode, SymbolTable symbolTable) {
        this.startNode = startNode;
        this.symbolTable = symbolTable;
        currentFunctionName = "main";
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
                    // COMMAND -> ASSIGN
                    if (currentNode.getChildren().get(0).getType().equals("ASSIGN")) {
                        return checkTypes(currentNode.getChildren().get(0));
                    } else if (currentNode.getChildren().get(0).getType().equals("BRANCH")) {
                        return checkTypes(currentNode.getChildren().get(0));
                    }
                    // add stuff for case COMMAND -> return ATOMIC
                    // COMMMAND -> return ATOMIC
                    if (currentNode.getChildren().get(0).getValue().equals("return")) {
                        String typeAtom = typeOfATOMIC(currentNode.getChildren().get(1));
                        // find node in symbol table

                        SymbolTable currTable = findSymbolTable(currentFunctionName, symbolTable);
                        currTable = currTable.getParent();

                        SymbolInfo si = currTable.lookup(currentFunctionName);
                        String functionType = si.type;

                        if (typeAtom.equals("n") && functionType.equals("num")) {
                            return true;
                        } else {
                            return false;
                        }
                    }

                    /// add functionallity for COMMAND -> CALL
                    // add fuinctionallity for COMMAND -> BRANCH
                }

            case "ATOMIC": // this case should not be reached
                return true;

            case "ASSIGN":

                // ASSIGN -> VNAME = TERM
                if (currentNode.getChildren().size() == 3
                        && currentNode.getChildren().get(2).getType().equals("TERM")) {
                    List<Node> childrenAssign = currentNode.getChildren();
                    String typeVn = typeOfVname(childrenAssign.get(0));
                    String typeTerm = typeOfTerm(childrenAssign.get(2));

                    if (typeVn.equals(typeTerm)) {
                        return true;
                    } else {
                        return false;
                    }

                } else {

                    // ASSIGN-> VNAME <input
                    List<Node> childrenAssign = currentNode.getChildren();
                    String typeVn = typeOfVname(childrenAssign.get(0));

                    if (typeVn.equals("n")) {

                        return true;
                    } else {
                        return false;
                    }
                }

                // return true;

            case "TERM": // should technically never reach here
                return true;
            case "CALL":
                return true;

            case "BRANCH": 
                // BRANCH -> if COND then ALGO1 else ALGO2
                List<Node> childrenBranch = currentNode.getChildren();
                String typeCond = typeOfCOND(childrenBranch.get(1));
                if(typeCond.equals("b")){
                    return checkTypes(childrenBranch.get(3)) && checkTypes(childrenBranch.get(5));
                }else{
                    return false;
                }

            case "FUNCTIONS":
                if (currentNode.getChildren().size() == 0) {
                    return true;
                } else {
                    //FUNCTIONS  -> DECL FUNCTIONS
                    List<Node> childrenFunctions = currentNode.getChildren();
                    boolean typeFunction = checkTypes(childrenFunctions.get(0));
                    boolean typeFunctions = checkTypes(childrenFunctions.get(1));

                    return (typeFunction && typeFunctions);
                }

            case "DECL":
                //DECL -> HEADER BODY
                return checkTypes(currentNode.getChildren().get(0)) && checkTypes(currentNode.getChildren().get(1));

            case "HEADER":
                //HEADER -> FTYP FNAME( VNAME1 , VNAME2 , VNAME3 )
                List<Node> childrenHeader = currentNode.getChildren();
                Node fname = childrenHeader.get(1);
                Node funcchild = fname.getChildren().get(0);
                String funcname = funcchild.getValue();
                currentFunctionName = funcname; //updating the current function name

                //normal processing

                Node vname1 = childrenHeader.get(3);
                Node vname2 = childrenHeader.get(5);
                Node vname3 = childrenHeader.get(7);

                if(typeOfVname(vname1).equals("n") && typeOfVname(vname2).equals("n") && typeOfVname(vname3).equals("n")){
                    return true;
                }else{
                    return false;
                }
               
            case "BODY":
                //BODY -> PROLOG LOCVARS ALGO EPILOG SUBFUNCS end
                List<Node> childrenBody = currentNode.getChildren();
                Node prolog = childrenBody.get(0);
                Node locvars = childrenBody.get(1);
                Node algo = childrenBody.get(2);
                Node epilog = childrenBody.get(3);
                Node subfuncs = childrenBody.get(4);

                return (checkTypes(prolog) && checkTypes(locvars) && checkTypes(algo) && checkTypes(epilog) && checkTypes(subfuncs));

            case "PROLOG":
                if(currentNode.getChildren().size() == 0){
                    return false;
                }else{
                    return true;
                }
            case "EPILOG":
                if(currentNode.getChildren().size() == 0){
                    return false;
                }else{
                    return true;
                }

            case "LOCAVARS":
                return true;
                
            case "SUBFUNCS":
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

    String typeOfVname(Node node) {

        SymbolTable currTable = findSymbolTable(currentFunctionName, symbolTable);
        Node children = node.getChildren().get(0);

        SymbolInfo si = currTable.lookup(children.getValue());

        String type = si.type;

        if (type.equals("num")) {
            return "n";
        } else {
            return "t";
        }

        // return "n";
    }

    String typeOfATOMIC(Node node) {
        Node children = node.getChildren().get(0);
        switch (children.getType()) {
            case "VNAME":
                return typeOfVname(children);
            case "CONST":
                return typeOfCONST(children);

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

    String typeOfTerm(Node node) {
        Node children = node.getChildren().get(0);

        switch (children.getType()) {
            case "ATOMIC":
                return typeOfATOMIC(children);
            case "CALL":
                return typeOfCall(children);
            case "OP":
                return typeOfOP(children);
        }

        return "";
    }

    String typeOfCall(Node node) {

        List<Node> children = node.getChildren();
        Node func = children.get(0);

        Node atom1 = children.get(2);
        Node atom2 = children.get(4);
        Node atom = children.get(6);

        String typeAtom1 = typeOfATOMIC(atom1);
        String typeAtom2 = typeOfATOMIC(atom2);
        String typeAtom = typeOfATOMIC(atom);

        if (typeAtom1.equals("n") && typeAtom2.equals("n") && typeAtom.equals("n")) {
            return typeOfFname(func);
        } else {
            return "u";
        }

    }

    String typeOfFname(Node node) {

        Node child = node.getChildren().get(0);
        String funcName = child.getValue();

        SymbolTable currTable = findSymbolTable(currentFunctionName, symbolTable);
        currTable = currTable.getParent();
        SymbolInfo si = currTable.lookup(funcName);
        String type = si.type;

        if (type.equals("num")) {
            return "n";
        } else {
            return "t";
        }

    }

    String typeOfOP(Node node) {

        List<Node> children = node.getChildren();
        // OP -> UNOP( ARG )
        if (children.get(0).getType().equals("UNOP")) {
            if (typeOfUNOP(children.get(0)).equals(typeOfARG(children.get(2)))
                    && typeOfUNOP(children.get(0)).equals("b")) {
                return "b";
            } else if (typeOfUNOP(children.get(0)).equals(typeOfARG(children.get(2)))
                    && typeOfUNOP(children.get(0)).equals("n")) {
                return "n";
            } else {
                return "u";
            }
        } else {
            // OP -> BINOP( ARG1 , ARG2 )
            Node binop = children.get(0);
            Node arg1 = children.get(2);
            Node arg2 = children.get(4);

            if (typeOFBINOP(binop).equals(typeOfARG(arg1)) && typeOFBINOP(binop).equals(typeOfARG(arg2))
                    && typeOFBINOP(binop).equals("b")) {
                return "b";
            } else if (typeOFBINOP(binop).equals(typeOfARG(arg1)) && typeOFBINOP(binop).equals(typeOfARG(arg2))
                    && typeOFBINOP(binop).equals("n")) {
                return "n";
            } else {
                return "u";
            }
        }

        // return "";
    }

    String typeOfUNOP(Node node) {

        Node child = node.getChildren().get(0);
        String value = child.getValue();

        if (value.equals("not")) {
            return "b";
        } else {
            return "n";
        }
        // return "";
    }

    String typeOfARG(Node node) {
        Node child = node.getChildren().get(0);

        switch (child.getType()) {
            case "ATOMIC":
                return typeOfATOMIC(child);
            case "OP":
                return typeOfOP(child);
        }
        return "";
    }

    String typeOFBINOP(Node node) {
        Node child = node.getChildren().get(0);

        if (child.getValue().equals("and") || child.getValue().equals("or")) {
            return "b";
        } else if (child.getValue().equals("add") || child.getValue().equals("sub") || child.getValue().equals("mul")
                || child.getValue().equals("div")) {
            return "n";
        } else {
            return "c";
        }
        // return "";
    }

    String typeOfCOND(Node node) {
        Node child = node.getChildren().get(0);

        if (child.getType().equals("SIMPLE")) {
            return typeOfSIMPLE(child);
        } else {
            return typeOfComposite(child);
        }

        // return "";
    }

    String typeOfSIMPLE(Node node) {
        // SIMPLE -> BINOP( ATOMIC1 , ATOMIC2 )
        List<Node> children = node.getChildren();

        Node binop = children.get(0);
        Node atomic1 = children.get(2);
        Node atomic2 = children.get(4);

        if (typeOFBINOP(binop).equals(typeOfATOMIC(atomic1)) && typeOFBINOP(binop).equals(typeOfATOMIC(atomic2))
                && typeOFBINOP(binop).equals("b")) {
            return "b";
        } else if (typeOFBINOP(binop).equals("c") && typeOfATOMIC(atomic1).equals(typeOfATOMIC(atomic2))
                && typeOfATOMIC(atomic1).equals("n")) {
            return "b";

        } else {
            return "u";
        }
        // return "";
    }

    String typeOfComposite(Node node) {
        List<Node> children = node.getChildren();

        if (children.get(0).equals("BINOP")) {

            // COMPOSIT->BINOP( SIMPLE1 , SIMPLE2 )
            Node binop = children.get(0);
            Node simple1 = children.get(2);
            Node simple2 = children.get(4);

            if( typeOFBINOP(binop).equals(typeOfSIMPLE(simple1)) && typeOFBINOP(binop).equals(typeOfSIMPLE(simple2)) && typeOFBINOP(binop).equals("b")){
                return "b";
            }else{
                return "u";
            }
        } else {
            // COMPOSIT -> UNOP ( SIMPLE )

            Node unop = children.get(0);
            Node simple = children.get(2);
            if(typeOfUNOP(unop).equals(typeOfSIMPLE(simple)) && typeOfUNOP(unop).equals("b")){
                return "b";
            }else{
                return "u";
            }
        }
        // return "";
    }

    String typeOfFTYP(Node node){

        Node child = node.getChildren().get(0);
        String value = child.getValue();

        if(value.equals("num")){
            return "n";
        }else{
            return "v";
        }
    }

    // add function to update current function name

    // create a method for each non-terminal in grammar.txt
    //

    // func for PROG
    // get the children

    // case 1:
    // case 2:

    public SymbolTable findSymbolTable(String tableName, SymbolTable root) {

        SymbolTable temp = root;
        if (temp.getName().equals(tableName)) {
            return temp;
        } else {
            for (SymbolTable child : temp.getChildren()) {
                SymbolTable res = findSymbolTable(tableName, child);
                if (res != null) {
                    return res;
                }
            }
            return null;
        }

    }
}
