import java.util.ArrayList;
import java.util.List;

public class Node {

    private String type; 
    private String value; 
    private List<Node> children; 

    public Node(String type){
        this.type = type; 
        this.children = new ArrayList<>(); 
    }

    public Node(String type, String value){
        this.type = type; 
        this.value = value; 
        this.children = new ArrayList<>(); 
    }

    public void addChild(Node n){
        this.children.add(n); 
    }

    public String getType(){
        return this.type; 
    }

    public String getValue(){
        return this.value; 
    }

    public List<Node> getChildren(){
        return this.children; 
    }

    @Override
    public String toString(){
        return "Node(type: "+type+ ", value: "+value+")"; 
    }



    
}
