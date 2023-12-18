package visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;

import java.util.ArrayList;

public class PostNodeVisitor extends ASTVisitor {

    public ArrayList<String> nodes;
    public PostNodeVisitor() {
        nodes = new ArrayList<>();
    }
    @Override
    public void postVisit(ASTNode node) {
        if (node.getNodeType() != ASTNode.SIMPLE_NAME)
            nodes.add(node.getNodeType() + "");
    }
}
