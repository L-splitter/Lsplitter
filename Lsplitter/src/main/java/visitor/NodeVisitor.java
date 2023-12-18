package visitor;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NodeVisitor extends ASTVisitor {
    public Map<String, List<Integer>> nodeToStartPosition;
    public NodeVisitor() {
        nodeToStartPosition = new HashMap<>();
    }

    public boolean visit(SimpleName node) {
        if (!nodeToStartPosition.containsKey(node.toString())) {
            nodeToStartPosition.put(node.toString(), new ArrayList<>());
        }
        nodeToStartPosition.get(node.toString()).add(node.getStartPosition());
        return true;
    }


}
