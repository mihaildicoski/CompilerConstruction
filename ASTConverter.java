import java.util.*;

public class ASTConverter {

    public String generateSyntaxTreeXml(Node rootNode) {
        StringBuilder xml = new StringBuilder();
        xml.append("<SYNTREE>\n");

        xml.append(generateRootXml(rootNode, 1));

        xml.append("<INNERNODES>\n");
        xml.append(generateInnerAndLeafNodesXml(rootNode, 1));
        xml.append("</INNERNODES>\n");

        xml.append("</SYNTREE>");
        return xml.toString();
    }

    private String generateRootXml(Node rootNode, int indentLevel) {
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<ROOT>\n");
        xml.append(indent).append("  <UNID>").append(rootNode.getId()).append("</UNID>\n");
        xml.append(indent).append("  <SYMB>").append(rootNode.getType()).append("</SYMB>\n");
        xml.append(indent).append("  <CHILDREN>\n");
        for (Node child : rootNode.getChildren()) {
            xml.append(indent).append("    <ID>").append(child.getId()).append("</ID>\n");
        }
        xml.append(indent).append("  </CHILDREN>\n");
        xml.append(indent).append("</ROOT>\n");

        return xml.toString();
    }

    private String generateInnerAndLeafNodesXml(Node node, int indentLevel) {
        StringBuilder xml = new StringBuilder();

        for (Node child : node.getChildren()) {
            if (child.getChildren().isEmpty()) {
                // leaf
                xml.append(generateLeafNodeXml(child, indentLevel + 1));
            } else {
                // inner
                xml.append(generateInnerNodeXml(child, indentLevel + 1));
                xml.append(generateInnerAndLeafNodesXml(child, indentLevel + 1));  // Recursively process children
            }
        }

        return xml.toString();
    }

    private String generateInnerNodeXml(Node node, int indentLevel) {
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<IN>\n");
        xml.append(indent).append("  <PARENT>").append(node.getParentId()).append("</PARENT>\n");
        xml.append(indent).append("  <UNID>").append(node.getId()).append("</UNID>\n");
        xml.append(indent).append("  <SYMB>").append(node.getType()).append("</SYMB>\n");
        xml.append(indent).append("  <CHILDREN>\n");
        for (Node child : node.getChildren()) {
            xml.append(indent).append("    <ID>").append(child.getId()).append("</ID>\n");
        }
        xml.append(indent).append("  </CHILDREN>\n");
        xml.append(indent).append("</IN>\n");

        return xml.toString();
    }

    private String generateLeafNodeXml(Node node, int indentLevel) {
        String indent = getIndent(indentLevel);

        StringBuilder xml = new StringBuilder();
        xml.append(indent).append("<LEAF>\n");
        xml.append(indent).append("  <PARENT>").append(node.getParentId()).append("</PARENT>\n");
        xml.append(indent).append("  <UNID>").append(node.getId()).append("</UNID>\n");
        xml.append(indent).append("  <TERMINAL>\n");
        xml.append(indent).append("    <TOKEN>").append(node.getValue()).append("</TOKEN>\n");
        xml.append(indent).append("  </TERMINAL>\n");
        xml.append(indent).append("</LEAF>\n");

        return xml.toString();
    }

    private String getIndent(int level) {
        return "  ".repeat(level);
    }
}
