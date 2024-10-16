import java.util.*; 

public class ScopeAnalyser {

    private Node rootNode; 
    private int vCounter = 0; 
    private int fCounter = 0; 
    private int nodeIdCounter = 1; 
    private ScopeStack scopeStack = new ScopeStack(); 

    private void assignIds(Node root){
        traverseAndAssign(root); 
    }

    private void traverseAndAssign(Node node){
        node.setId(nodeIdCounter++);

        for(Node child: node.getChildren()){
            traverseAndAssign(child);
        }
    }

    public ScopeAnalyser(Node root){
        this.rootNode = root; 
        assignIds(rootNode);
        printAST(rootNode, " ");
    }

    public void printAST(Node node, String indent) {
        if (node == null) return;
        System.out.println(indent + "ID: "+ node.getId() + " " + node.getType() + (node.getValue() != null ? ": " + node.getValue() : ""));
        for (Node child : node.getChildren()) {
            printAST(child, indent + "  ");
        }
    }
    
}


class ScopeStack{
    private Stack<SymbolTable> scopeStack = new Stack<>(); 
}

class SymbolTable{
    private Map<String, SymbolInfo> table = new HashMap<>();

    public void addSymbol(String originalName, String uniqueName, String type, int nodeId) {
        table.put(originalName, new SymbolInfo(uniqueName, type, nodeId));
    }

    public SymbolInfo lookup(String name) {
        return table.get(name);  // Return null if not found
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