import java.util.*; 

public class ASTConverter {

    private int nodeCounter = 0; 
    private Map<Node, Integer> nodeToIdMap = new HashMap<>();

    public String generateSyntaxTreeXml(Node rootNode) {
        StringBuilder xml = new StringBuilder();
        xml.append("<SYNTREE>\n");

        // Generate the root XML
        xml.append(generateRootXml(rootNode, 1));

        // Generate the inner nodes and leaf nodes
        xml.append("<INNERNODES>\n");
        xml.append(generateInnerAndLeafNodesXml(rootNode, 1));
        xml.append("</INNERNODES>\n");

        xml.append("</SYNTREE>");
        return xml.toString();
    }

    private String generateRootXml(Node rootNode, int indentLevel) {
        int rootUnid = getUnid(rootNode);  // Assign and get the unique ID for the root node
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<ROOT>\n");
        xml.append(indent).append("  <UNID>").append(rootUnid).append("</UNID>\n");
        xml.append(indent).append("  <SYMB>").append(rootNode.getType()).append("</SYMB>\n");
        xml.append(indent).append("  <CHILDREN>\n");
        for (Node child : rootNode.getChildren()) {
            xml.append(indent).append("    <ID>").append(getUnid(child)).append("</ID>\n");
        }
        xml.append(indent).append("  </CHILDREN>\n");
        xml.append(indent).append("</ROOT>\n");

        return xml.toString();
    }

    private String generateInnerAndLeafNodesXml(Node node, int indentLevel) {
        StringBuilder xml = new StringBuilder();

        for (Node child : node.getChildren()) {
            if (child.getChildren().isEmpty()) {
                // It's a leaf node
                xml.append(generateLeafNodeXml(child, indentLevel + 1));
            } else {
                // It's an inner node
                xml.append(generateInnerNodeXml(child, indentLevel + 1));
                xml.append(generateInnerAndLeafNodesXml(child, indentLevel + 1));  // Recursively process children
            }
        }

        return xml.toString();
    }

    private String generateInnerNodeXml(Node node, int indentLevel) {
        int nodeUnid = getUnid(node);
        int parentUnid = getParentUnid(node);
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<IN>\n");
        xml.append(indent).append("  <PARENT>").append(parentUnid).append("</PARENT>\n");
        xml.append(indent).append("  <UNID>").append(nodeUnid).append("</UNID>\n");
        xml.append(indent).append("  <SYMB>").append(node.getType()).append("</SYMB>\n");
        xml.append(indent).append("  <CHILDREN>\n");
        for (Node child : node.getChildren()) {
            xml.append(indent).append("    <ID>").append(getUnid(child)).append("</ID>\n");
        }
        xml.append(indent).append("  </CHILDREN>\n");
        xml.append(indent).append("</IN>\n");

        return xml.toString();
    }

    private String generateLeafNodeXml(Node node, int indentLevel) {
        int nodeUnid = getUnid(node);
        int parentUnid = getParentUnid(node);
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<LEAF>\n");
        xml.append(indent).append("  <PARENT>").append(parentUnid).append("</PARENT>\n");
        xml.append(indent).append("  <UNID>").append(nodeUnid).append("</UNID>\n");
        xml.append(indent).append("  <TERMINAL>\n");
        xml.append(indent).append("    <TOKEN>").append(node.getValue()).append("</TOKEN>\n");
        xml.append(indent).append("  </TERMINAL>\n");
        xml.append(indent).append("</LEAF>\n");

        return xml.toString();
    }

    private int getUnid(Node node) {
        if (!nodeToIdMap.containsKey(node)) {
            nodeToIdMap.put(node, nodeCounter++);  // Assign a new unique ID
        }
        return nodeToIdMap.get(node);
    }

    private int getParentUnid(Node node) {
        for (Map.Entry<Node, Integer> entry : nodeToIdMap.entrySet()) {
            Node parentNode = entry.getKey();
            if (parentNode.getChildren().contains(node)) {
                return entry.getValue();  
            }
        }
        return -1;  
    }

    private String getIndent(int level) {
        return "  ".repeat(level);  
    }

    
}
