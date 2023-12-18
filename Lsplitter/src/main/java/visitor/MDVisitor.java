package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.util.HashMap;
import java.util.Map;

public class MDVisitor extends ASTVisitor {
    public Map<String, methodInfomation> methods;
    public MDVisitor() {
        methods = new HashMap<>();
    }

    public boolean visit(MethodDeclaration node) {
        String comment = "";
        if (node.getJavadoc() != null) {
            comment = node.getJavadoc().toString();
        }
        methods.put(node.getName().toString(), new methodInfomation(node.getName().toString(), node.toString(), comment, node.getStartPosition(), node.getStartPosition() + node.getLength(), node.getReturnType2() == null ? "" : node.getReturnType2().toString()));
        return true;
    }



}
