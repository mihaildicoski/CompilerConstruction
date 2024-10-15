import java.util.ArrayList;
import java.util.List;

public class Node {

    private String type; 
    private String value; 
    private List<Node> children; 
    private int id;
    private int parentId; 


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
        n.setParentId(this.id); 
        this.children.add(n); 
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
