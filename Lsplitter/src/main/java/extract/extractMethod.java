package extract;

import chatGPT.queryGPT;
import chatGPT.queryGPT4;
import gr.uom.java.xmi.diff.ExtractOperationRefactoring;
import org.eclipse.jdt.core.dom.*;
import org.jetbrains.annotations.NotNull;
import org.refactoringminer.api.GitHistoryRefactoringMiner;
import org.refactoringminer.api.Refactoring;
import org.refactoringminer.api.RefactoringHandler;
import org.refactoringminer.api.RefactoringType;
import org.refactoringminer.rm1.GitHistoryRefactoringMinerImpl;

import java.io.File;
import java.io.IOException;
import java.util.*;

import util.Utils;
import util.changeInfomation;
import visitor.*;

class lineAndInvocation {
    public int line;
    public String InvocationName;
    public int startPostion, endPostion;
    public List<String> arguments;
    public lineAndInvocation(int line, String InvocationName, int startPostion, int endPostion) {
        this.line = line;
        this.InvocationName = InvocationName;
        this.startPostion = startPostion;
        this.endPostion = endPostion;
        this.arguments = new ArrayList<String>();
    }
}
public class extractMethod {
    String oldMethod;
    String apiKey;
    String apiUrl;
    String gptMethod;
    String resMethod;
    public extractMethod(File oldMethod, String apiKey, String apiUrl, File gptMethod) throws IOException {
        this.apiKey = apiKey;
        this.apiUrl = apiUrl;
        this.oldMethod = Utils.readFromFile(oldMethod);
        this.gptMethod = Utils.readFromFile(gptMethod);
        this.resMethod = null;
    }
    public void getExtractedMethod() throws InterruptedException {
        String newMethod = gptMethod;
        String oldMethod = this.oldMethod;
        if (newMethod == null) {
            String promot = "Suppose you are a skilled software engineer and now you should refactor your code. Please decompose the following long method into small ones, but do not modify the signature of the original method. Please give me the full resulting methods with comment for each method in Javadoc format. The new methods should have unique method names. Please avoid tiny methods, empty methods, or new classes. Don't generate code summary.\n";
            String res = queryGPT4.query(apiKey, apiUrl, promot + "```java\n" + oldMethod.trim() + "\n```\n");
            // System.out.println("ChatGPT REPLY:\n" + res);
            newMethod = Utils.getCodeFromAnswer(res);
        }
        // Using refacting miner to get Result
        newMethod = Utils.removeEmptyLines(newMethod);
        gptMethod = newMethod;
        oldMethod = "class test {\n" + oldMethod + "\n}\n";
        while (true) {
            newMethod = "class test {\n" + newMethod + "\n}\n";
            //  remove empty methods
            List<Refactoring> extractedMethods = getExtractedMethods(oldMethod, newMethod);
            // get all methods and these comment
            Map<String, methodInfomation> methods = getNameToComment(newMethod);
            List<lineAndInvocation> invocations = new ArrayList<>();
            for (int j = 0; j < extractedMethods.size(); j++) {
                ExtractOperationRefactoring ref = (ExtractOperationRefactoring)extractedMethods.get(j);
                if (j > 0) {
                    ExtractOperationRefactoring lastref = (ExtractOperationRefactoring) extractedMethods.get(j - 1);
                    if (ref.getExtractedOperationInvocations().get(0).getName().equals(lastref.getExtractedOperationInvocations().get(0).getName())) {
                        continue;
                    }
                }
                if (j < extractedMethods.size() - 1) {
                    ExtractOperationRefactoring afterref = (ExtractOperationRefactoring) extractedMethods.get(j + 1);
                    if (ref.getExtractedOperationInvocations().get(0).getName().equals(afterref.getExtractedOperationInvocations().get(0).getName())) {
                        continue;
                    }
                }
                if (ref.getExtractedOperationInvocations().size() == 1) {
                    invocations.add(new lineAndInvocation(ref.getExtractedOperationInvocations().get(0).getLocationInfo().getStartLine(),
                            ref.getExtractedOperationInvocations().get(0).getName(), ref.getExtractedOperationInvocations().get(0).getLocationInfo().getStartOffset(),
                            ref.getExtractedOperationInvocations().get(0).getLocationInfo().getEndOffset()));
                    for (int i = 0; i < ref.getExtractedOperationInvocations().get(0).arguments().size(); i++) {
                        invocations.get(invocations.size() - 1).arguments.add(ref.getExtractedOperationInvocations().get(0).arguments().get(i).toString());
                    }
                }
            }
            Collections.sort(invocations, Comparator.comparingInt(x -> x.line));
            int n = invocations.size();
            double[][] sim = new double[n][n];
            for (int i = 0; i < invocations.size(); i++) {
                for (int j = i + 1; j < invocations.size(); j++) {
                    sim[i][j] = calcSim(methods.get(invocations.get(i).InvocationName), methods.get(invocations.get(j).InvocationName));
                    sim[j][i] = sim[i][j];
                }
            }

            double curBest = 0; int st = -1, en = -1;
            for (int i = 0; i < invocations.size(); i++) {
                int totNCSS = countNCSS(methods.get(invocations.get(i).InvocationName).body);
                for (int j = i + 1; j < invocations.size(); j++) {
                    if (invocations.get(j).line != invocations.get(j - 1).line + 1) break;
                    totNCSS += countNCSS(methods.get(invocations.get(j).InvocationName).body);
                    if (totNCSS >= 60) break;
                    double tot = 0;
                    // i ~ j
                    for (int k = i; k <= j; k++) {
                        for (int t = k + 1; t <= j; t++) {
                            tot += sim[k][t];
                        }
                    }
                    tot /= (j - i + 1) * (j - i) / 2.0;
                    if (tot > curBest) {
                        curBest = tot;
                        st = i; en = j + 1;
                    }
                }
            }

            List<changeInfomation> changes = new ArrayList<>();
            List<String> addCodes = new ArrayList<>();
            if (curBest > 2) {
                System.out.println("Use MERGE METHODS");
                List<methodInfomation> mergeMethods = new ArrayList<>();
                StringBuilder methodTot = new StringBuilder();
                StringBuilder invocationTot = new StringBuilder();
                for (int i = st; i < en; i++) {
                    lineAndInvocation invocation = invocations.get(i);
                    methodInfomation minfo = methods.get(invocation.InvocationName);
                    methodTot.append(minfo.body.trim()).append("\n");
                    invocationTot.append(newMethod.substring(invocation.startPostion, invocation.endPostion + 1)).append("\n");
                }
                String promotMerge = "Suppose you are a skilled software engineer and now you should refactor your code. Now here are some java methods, please merge them into one method. Don't generate code summary. Only give me the new method.\nHere we present the to-be-merged methods in markdown format.\n";
                String resMerge = queryGPT4.query(apiKey, apiUrl, promotMerge + "```java\n" + methodTot.toString().trim() + "\n```\n");
                String mergedMethod = Utils.getCodeFromAnswer(resMerge);
                mergedMethod = Utils.removeEmptyLines(mergedMethod);
                String promotInvocation1 = "Suppose you are a skilled software engineer and now you should refactor your code. You have merged these methods into one method. Here we present the old methods in markdown format.\n";
                String promotInvocation2 = "You merge them into this method. Here we present the merged methods in markdown format.\n";
                String promotInvocation3 = "Now please give me the new method invocation for it. The original method invocations are:\n";
                String promotInvocation4 = "Don't generate code summary. Only give me the new method invocation.";
                String resInvoation = queryGPT4.query(apiKey, apiUrl, promotInvocation1 + "```java\n" + methodTot.toString().trim() + "\n```\n" + promotInvocation2 +
                        "```java\n" + mergedMethod.trim() + "\n```\n" + promotInvocation3 + "```java\n" + invocationTot.toString().trim() + "\n```\n" + promotInvocation4);
                String mergedInvocation = Utils.getCodeFromAnswer(resInvoation);
                mergedInvocation = Utils.removeEmptyLines(mergedInvocation);
                addCodes.add(mergedMethod);
                changes.add(new changeInfomation(invocations.get(st).startPostion, invocations.get(en - 1).endPostion + 1, mergedInvocation));
                for (int i = st; i < en; i++) {
                    methodInfomation minfo = methods.get(invocations.get(i).InvocationName);
                    changes.add(new changeInfomation(minfo.startPosition, minfo.endPosition, ""));
                }
            } else {
                newMethod = newMethod.substring(newMethod.indexOf("{") + 1, newMethod.lastIndexOf("}"));
                break;
            }
            newMethod = Utils.changeString(newMethod, changes);
            newMethod = newMethod.substring(newMethod.indexOf("{") + 1, newMethod.lastIndexOf("}"));
            for (String add: addCodes) {
                newMethod += "\n" + add + "\n";
            }
        }
        while (true) {
            newMethod = "class test {\n" + newMethod + "\n}\n";
            //  remove empty methods
            List<Refactoring> extractedMethods = getExtractedMethods(oldMethod, newMethod);
            // get all method and these comment
            Map<String, methodInfomation> methods = getNameToComment(newMethod);
            List<lineAndInvocation> invocations = new ArrayList<>();
            for (int j = 0; j < extractedMethods.size(); j++) {
                ExtractOperationRefactoring ref = (ExtractOperationRefactoring) extractedMethods.get(j);
                if (j > 0) {
                    ExtractOperationRefactoring lastref = (ExtractOperationRefactoring) extractedMethods.get(j - 1);
                    if (ref.getExtractedOperationInvocations().get(0).getName().equals(lastref.getExtractedOperationInvocations().get(0).getName())) {
                        continue;
                    }
                }
                if (j < extractedMethods.size() - 1) {
                    ExtractOperationRefactoring afterref = (ExtractOperationRefactoring) extractedMethods.get(j + 1);
                    if (ref.getExtractedOperationInvocations().get(0).getName().equals(afterref.getExtractedOperationInvocations().get(0).getName())) {
                        continue;
                    }
                }
                if (ref.getExtractedOperationInvocations().size() == 1) {
                    invocations.add(new lineAndInvocation(ref.getExtractedOperationInvocations().get(0).getLocationInfo().getStartLine(),
                            ref.getExtractedOperationInvocations().get(0).getName(), ref.getExtractedOperationInvocations().get(0).getLocationInfo().getStartOffset(),
                            ref.getExtractedOperationInvocations().get(0).getLocationInfo().getEndOffset()));
                    for (int i = 0; i < ref.getExtractedOperationInvocations().get(0).arguments().size(); i++) {
                        invocations.get(invocations.size() - 1).arguments.add(ref.getExtractedOperationInvocations().get(0).arguments().get(i).toString());
                    }
                }
            }
            Collections.sort(invocations, Comparator.comparingInt(x -> x.line));
            List<changeInfomation> changes = new ArrayList<>();
            List<String> addCodes = new ArrayList<>();
            for (int i = 0; i < invocations.size(); i++) {
                methodInfomation methodInfo = methods.get(invocations.get(i).InvocationName);
                ASTParser parser = ASTParser.newParser(AST.JLS19);
                parser.setSource(methodInfo.body.toCharArray());
                parser.setKind(ASTParser.K_CLASS_BODY_DECLARATIONS);

                TypeDeclaration typeDeclaration = (TypeDeclaration) parser.createAST(null);
                MethodDeclaration[] methodDeclaration = typeDeclaration.getMethods();
                MethodDeclaration md = methodDeclaration[0];
                if (md.getReturnType2().toString().equals("void")) {
                    if (countNCSS(methodInfo.body) <= 2) {
                        System.out.println("USE VOID MERGE");
                        String changedCode = getCodeToChange(invocations.get(i), methodInfo, md);
                        changedCode = changedCode.substring(changedCode.indexOf("{") + 1, changedCode.lastIndexOf("}"));
                        changes.add(new changeInfomation(invocations.get(i).startPostion, invocations.get(i).endPostion + 1, changedCode));
                        changes.add(new changeInfomation(methodInfo.startPosition, methodInfo.endPosition, ""));
                        break;
                    }
                } else { // B. if has return val. only a return statement
                    if (md.getBody().statements().size() == 1 && md.getBody().statements().get(0).toString().startsWith("return")) {
                        System.out.println("USE RETURN MERGE");
                        String changedCode = getCodeToChange(invocations.get(i), methodInfo, md);
                        changedCode = "(" + changedCode.substring(changedCode.lastIndexOf("return") + 6, changedCode.lastIndexOf(";")) + ")";
                        changes.add(new changeInfomation(invocations.get(i).startPostion, invocations.get(i).endPostion, changedCode));
                        changes.add(new changeInfomation(methodInfo.startPosition, methodInfo.endPosition, ""));
                        break;
                    }
                }
            }
            if (addCodes.size() == 0 && changes.size() == 0) {
                newMethod = newMethod.substring(newMethod.indexOf("{") + 1, newMethod.lastIndexOf("}"));
                break;
            }
            newMethod = Utils.changeString(newMethod, changes);
            newMethod = newMethod.substring(newMethod.indexOf("{") + 1, newMethod.lastIndexOf("}"));
            for (String add : addCodes) {
                newMethod += "\n" + add + "\n";
            }
        }
        resMethod = newMethod;
//        newMethod = "class test {\n" + newMethod + "\n}\n";
//        newMethod = Utils.formatCode(newMethod);
//        return newMethod.substring(newMethod.indexOf("{") + 1, newMethod.lastIndexOf("}"));
    }
    private int countNCSS(String methodBody) {
        int cnt = 0;
        for (char ch: methodBody.toCharArray()) {
            if (ch == ';') {
                cnt++;
            }
        }
        return cnt;
    }
    private double calcSim(methodInfomation methodA, methodInfomation methodB) {
        double sim = 0;
        String nameA = methodA.name;
        String nameB = methodB.name;
        double nameSimilar = wordSimilar(nameA, nameB);
        double commentSimilar = 0;
        String commentA = methodA.comment;
        String commentB = methodB.comment;
        if (commentA.equals("") || commentB.equals("")) {
            commentSimilar = 0;
        } else {
            commentA = Utils.getComment(commentA);
            commentB = Utils.getComment(commentB);
            commentSimilar = wordSimilar(commentA, commentB);
        }
        sim += Math.max(nameSimilar, commentSimilar);

        String mA = "class test { + \n" + methodA.body + "\n}";
        ASTParser astParserA = Utils.getNewASTParser();
        astParserA.setSource(mA.toCharArray());
        CompilationUnit cuA = (CompilationUnit) astParserA.createAST(null);
        SimpleNameVisitor visitorA = new SimpleNameVisitor();
        cuA.accept(visitorA);

        String mB = "class test { + \n" + methodB.body + "\n}";
        ASTParser astParserB = Utils.getNewASTParser();
        astParserB.setSource(mB.toCharArray());
        CompilationUnit cuB = (CompilationUnit) astParserB.createAST(null);
        SimpleNameVisitor visitorB = new SimpleNameVisitor();
        cuB.accept(visitorB);

        int numA = visitorA.names.size(), numB = visitorB.names.size();
        visitorA.names.retainAll(visitorB.names);
        sim += 2.0 * (visitorA.names.size()) / (numA + numB);

        astParserA = Utils.getNewASTParser();
        astParserA.setSource(mA.toCharArray());
        cuA = (CompilationUnit) astParserA.createAST(null);
        PostNodeVisitor visitorA1 = new PostNodeVisitor();
        cuA.accept(visitorA1);

        astParserB = Utils.getNewASTParser();
        astParserB.setSource(mB.toCharArray());
        cuB = (CompilationUnit) astParserB.createAST(null);
        PostNodeVisitor visitorA2 = new PostNodeVisitor();
        cuB.accept(visitorA2);

        sim += 2.0 * (longestCommonSubsequence(visitorA1.nodes, visitorA2.nodes)) / (visitorA1.nodes.size() + visitorA2.nodes.size());

        return sim;
    }
    private static String getCodeToChange(lineAndInvocation invocation, methodInfomation method, MethodDeclaration md) {
        List<changeInfomation> changes = new ArrayList<>();
        Map<String, String> newToOld = new HashMap<>();
        for (int i = 0; i < md.parameters().size(); i++) {
            newToOld.put(((VariableDeclaration)(md.parameters().get(i))).getName().toString(), invocation.arguments.get(i));
        }
        String body = md.toString();
        NodeVisitor visitor = new NodeVisitor();
        md.accept(visitor);
        Map<String, List<Integer>> nodeToStartPosition = visitor.nodeToStartPosition;
        for (String key: nodeToStartPosition.keySet()) {
            if (newToOld.containsKey(key)) {
                for (int sp: nodeToStartPosition.get(key)) {
                    changes.add(new changeInfomation(sp, sp + key.length(), newToOld.get(key)));
                }
            }
        }
        body = Utils.changeString(body, changes);
        return body;
    }

    private static int longestCommonSubsequence(ArrayList<String> A, ArrayList<String> B) {
        int lenA = A.size();
        int lenB = B.size();
        if (lenA == 0 || lenB == 0) {
            return 0;
        }

        int[][] res = new int[lenA + 1][lenB + 1];

        for (int i = 1; i <= lenA; i++) {
            for (int j = 1; j <= lenB; j++) {
                res[i][j] = Math.max(res[i][j - 1], res[i - 1][j]);
                if (A.get(i - 1).equals(B.get(j - 1))) {
                    res[i][j] = Math.max(res[i][j], res[i - 1][j - 1] + 1);
                }
            }
        }

        return res[lenA][lenB];
    }

    private static ArrayList<String> splitWord(String str) {
        ArrayList<String> result = new ArrayList<>();
        int start = 0;
        for (int i = 0; i < str.length(); i++) {
            char now = str.charAt(i);
            if (!(('a' <= now && now <= 'z') || ('0' <= now && now <= '9'))) {
                if (start != i) {
                    result.add(str.substring(start, i).toLowerCase());
                }
                start = i;
                if (now < 'A' || now > 'Z') start++;
            }
        }
        if (start != str.length()) {
            result.add(str.substring(start).toLowerCase());
        }
        return result;
    }

    private double wordSimilar(String nameA, String nameB) {
        ArrayList<String> wordsA = splitWord(nameA);
        ArrayList<String> wordsB = splitWord(nameB);
        int lcs = longestCommonSubsequence(wordsA, wordsB);
        return 2.0 * lcs / (wordsA.size() + wordsB.size());
    }

    private static boolean isSimilar(String nameA, String nameB, double threshold) {
        ArrayList<String> wordsA = splitWord(nameA);
        ArrayList<String> wordsB = splitWord(nameB);
        int lcs = longestCommonSubsequence(wordsA, wordsB);
        return 1.0 * lcs / Math.min(wordsA.size(), wordsB.size()) >= threshold;
    }

    private static Map<String, methodInfomation> getNameToComment(String method) {
        ASTParser astParser = Utils.getNewASTParser();
        astParser.setSource(method.toCharArray());
        CompilationUnit cu = (CompilationUnit) astParser.createAST(null);
        MDVisitor visitor = new MDVisitor();
        cu.accept(visitor);
        return visitor.methods;
    }

    private static List<Refactoring> getExtractedMethods(String oldMethod, String newMethod) {
        GitHistoryRefactoringMiner miner = new GitHistoryRefactoringMinerImpl();
        Map<String, String> fileContentsBefore = new HashMap<>();
        Map<String, String> fileContentsAfter = new HashMap<>();
        fileContentsBefore.put("src/test.java", oldMethod);
        fileContentsAfter.put("src/test.java", newMethod);
        // populate the maps
        List<Refactoring> refs = new ArrayList<>();

        miner.detectAtFileContents(fileContentsBefore, fileContentsAfter, new RefactoringHandler() {
            @Override
            public void handle(String commitId, List<Refactoring> refactorings) {
                for (Refactoring ref : refactorings) {
                    if (ref.getRefactoringType() == RefactoringType.EXTRACT_OPERATION) {
                        // System.out.println(ref.toString());
                        refs.add(ref);
                    }
                }
            }
        });
        return refs;
    }
}
