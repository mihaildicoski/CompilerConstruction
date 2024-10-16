import java.util.ArrayList;
import java.util.List;

public class Node {

    private String type; 
    private String value; 
    private List<Node> children; 
    private int id;
    private int parentId; 
    private Node parent; 


    public Node(String type, int id){
        this.type = type; 
        this.children = new ArrayList<>(); 
        this.id = id; 
    }

    public Node(String type, String value, int id){
        this.type = type; 
        this.value = value; 
        this.children = new ArrayList<>(); 
        this.id = id; 
    }

    public void addChild(Node n){
        n.setParentId(this.id); 
        this.children.add(n);
        n.setParent(this);  
    }

    public void setId(int id){
        this.id = id; 
    }

    public void setParentId(int id){
        this.parentId = id; 
    }

    public int getId(){
        return this.id; 
    }
    
    public int getParentId(){
        return this.parentId; 
    }


    public Node getNode(int id, Node node){
        if(node.getId() == id){
            return node; 
        }
        for(Node child: node.getChildren()){
            getNode(id, child); 
        }
        return null; 
        
    }

    public void setParent(Node parent){
        this.parent = parent; 
    }

    public Node getParent(){
        return this.parent; 
    }

    public String getType(){
        return this.type; 
    }

    public String getValue(){
        return this.value; 
    }

    public void setValue(String value){
        this.value = value; 
    }


    public List<Node> getChildren(){
        return this.children; 
    }

    @Override
    public String toString(){
        return "Node(id: "+id+ "type: "+type+ ", value: "+value+")"; 
    }

    public String toXml() {
        StringBuilder xml = new StringBuilder();
        if (children.isEmpty()) {
            // leaf
            xml.append("<LEAF>\n");
            xml.append("<PARENT>").append(parentId).append("</PARENT>\n");
            xml.append("<UNID>").append(id).append("</UNID>\n");
            xml.append("<TERMINAL>\n");
            xml.append("<TOKEN>").append(value).append("</TOKEN>\n");  
            xml.append("</TERMINAL>\n");
            xml.append("</LEAF>\n");
        } else {
            //inner
            xml.append("<IN>\n");
            xml.append("<PARENT>").append(parentId).append("</PARENT>\n");
            xml.append("<UNID>").append(id).append("</UNID>\n");
            xml.append("<SYMB>").append(type).append("</SYMB>\n");
            xml.append("<CHILDREN>\n");
            for (Node child : children) {
                xml.append("<ID>").append(child.getId()).append("</ID>\n");
            }
            xml.append("</CHILDREN>\n");
            xml.append("</IN>\n");
        }
        return xml.toString();
    }



    
}
