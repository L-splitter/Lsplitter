package visitor;

import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SimpleNameVisitor extends ASTVisitor {
    public Set<String> names;
    public SimpleNameVisitor() {
        names = new HashSet<>();
    }

    public boolean visit(SimpleName node) {
        names.add(node.toString());
        return true;
    }



}
