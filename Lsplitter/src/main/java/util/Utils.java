package util;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;

import java.io.*;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class Utils {
    public static String getCodeFromAnswer(String answer) {
        if ((answer.contains("```"))) {
            if (answer.contains("```java")) {
                if (countSubstringOccurrences(answer, "```") > 1)
                    return answer.substring(answer.indexOf("```java") + 7, answer.lastIndexOf("```")).trim();
                else
                    return answer.substring(answer.indexOf("```java") + 7).trim();
            } else {
                if (countSubstringOccurrences(answer, "```") > 1)
                    return answer.substring(answer.indexOf("```") + 3, answer.lastIndexOf("```")).trim();
                else
                    return answer.substring(answer.indexOf("```") + 3).trim();
            }
        } else {
            return answer.trim();
        }
    }
    public static int countSubstringOccurrences(String text, String pattern) {
        int count = 0;
        int index = 0;

        // 使用indexOf找到子串的位置，每次找到后更新查找起点
        while ((index = text.indexOf(pattern, index)) != -1) {
            count++;
            index += pattern.length(); // 更新查找起点
        }

        return count;
    }
    public static ASTParser getNewASTParser() {
        ASTParser astParser;
        astParser = ASTParser.newParser(AST.JLS19);
        astParser.setResolveBindings(false);
        astParser.setStatementsRecovery(true);
        astParser.setKind(ASTParser.K_COMPILATION_UNIT);
        return astParser;
    }
    public static String removeEmptyLines(String text) {
        String[] lines = text.split("\n");
        StringBuilder result = new StringBuilder();

        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                result.append(line).append("\n");
            }
        }

        // Remove the last newline character if present
        if (result.length() > 0 && result.charAt(result.length() - 1) == '\n') {
            result.deleteCharAt(result.length() - 1);
        }

        return result.toString();
    }

    public static String changeString(String s, List<changeInfomation> changes) {
        Collections.sort(changes, Comparator.comparingInt(x -> x.start));
        StringBuilder result = new StringBuilder();
        int last = 0;
        for (changeInfomation change: changes) {
            result.append(s.substring(last, change.start));
            last = change.end;
            result.append(change.newCode);
        }
        result.append(s.substring(last));
        return result.toString();
    }

    public static String formatCode(String code) {
        ASTParser astParser = Utils.getNewASTParser();
        astParser.setSource(code.toCharArray());
        CompilationUnit cu = (CompilationUnit) astParser.createAST(null);
        return cu.toString();
    }

    public static String getComment(String javadoc) {
        String[] comments = javadoc.split("\n");
        StringBuilder result = new StringBuilder();
        for (String comment: comments) {
            if (comment.startsWith(" * ") && !comment.startsWith(" * @")) {
                result.append(comment.substring(3));
            }
        }
        return result.toString();
    }

    public static String readFromFile(File file) throws IOException {
        if (file == null) {
            return null;
        }
        FileReader reader = new FileReader(file);
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader br = new BufferedReader(reader);
        while ((line = br.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    public static void writeToFile(String things, File file) throws FileNotFoundException, UnsupportedEncodingException {
        PrintWriter out = new PrintWriter(file, "UTF-8");
        out.println(things.trim());
        out.close();
    }
}
