package visitor;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class methodInfomation {
    public String name;
    public String body;
    public String comment;
    public int startPosition;
    public int endPosition;
    public String returnType;

    public methodInfomation(String name, String body, String comment, int startPosition, int endPosition, String returnType) {
        this.name = name;
        this.body = body;
        this.comment = comment;
        this.startPosition = startPosition;
        this.endPosition = endPosition;
        this.returnType = returnType;
    }

}
