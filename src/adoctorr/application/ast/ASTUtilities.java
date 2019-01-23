package adoctorr.application.ast;

import org.eclipse.jdt.core.dom.*;
import parser.CodeParser;
import parser.MethodVisitor;
import process.FileUtilities;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ASTUtilities {

    public static CompilationUnit getCompilationUnit(File sourceFile) throws IOException {
        CodeParser codeParser = new CodeParser();
        String javaFileContent = FileUtilities.readFile(sourceFile.getAbsolutePath());
        return codeParser.createParser(javaFileContent);
    }

    public static MethodDeclaration getMethodDeclarationFromName(String methodName, CompilationUnit compilationUnit) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
        ArrayList<MethodDeclaration> methodDeclarationList = new ArrayList<>();
        // Fetch all MethodDeclarations of the class with an AST visitor of aDoctor
        typeDeclaration.accept(new MethodVisitor(methodDeclarationList));

        // Fetch the correct MethodDeclaration through a comparison with the content of the MethodBean parameter
        int i = 0;
        int methodDeclarationListSize = methodDeclarationList.size();
        boolean found = false;
        while (i < methodDeclarationListSize && !found) {
            if (methodDeclarationList.get(i).getName().toString().equals(methodName)) {
                found = true;
            } else {
                i++;
            }
        }
        if (found) {
            return methodDeclarationList.get(i);
        } else {
            return null;
        }
    }

    public static MethodDeclaration getMethodDeclarationFromContent(String methodContent, CompilationUnit compilationUnit) {
        TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
        ArrayList<MethodDeclaration> methodDeclarationList = new ArrayList<>();
        // Fetch all MethodDeclarations of the class with an AST visitor of aDoctor
        typeDeclaration.accept(new MethodVisitor(methodDeclarationList));

        // Fetch the correct MethodDeclaration through a comparison with the content of the MethodBean parameter
        int i = 0;
        int methodDeclarationListSize = methodDeclarationList.size();
        boolean found = false;
        while (i < methodDeclarationListSize && !found) {
            if (methodDeclarationList.get(i).toString().equals(methodContent)) {
                found = true;
            } else {
                i++;
            }
        }
        if (found) {
            return methodDeclarationList.get(i);
        } else {
            return null;
        }
    }

    public static ArrayList<Block> getBlocksInMethod(MethodDeclaration methodDeclaration) {
        if (methodDeclaration == null) {
            return null;
        } else {
            ArrayList<Block> blockList = new ArrayList<>();
            methodDeclaration.accept(new BlockVisitor(blockList));
            return blockList;
        }
    }

    public static Block getBlockFromContent(String blockContent, MethodDeclaration methodDeclaration) {
        // Fetch all Blocks
        ArrayList<Block> blockList = new ArrayList<>();
        methodDeclaration.accept(new BlockVisitor(blockList));
        int i = 0;
        int blockListSize = blockList.size();
        while (i < blockListSize && !blockList.get(i).toString().equals(blockContent)) {
            i++;
        }
        if (i < blockListSize) {
            return blockList.get(i);
        } else {
            return null;
        }
    }

    public static FieldDeclaration getFieldDeclarationFromName(String variableName, CompilationUnit compilationUnit) {
        // Fetch all FieldDeclaration of the class
        TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
        ArrayList<FieldDeclaration> fieldDeclarationList = new ArrayList<>();
        typeDeclaration.accept(new FieldDeclarationVisitor(fieldDeclarationList));
        for (FieldDeclaration fieldDeclaration : fieldDeclarationList) {
            List fragments = fieldDeclaration.fragments();
            if (fragments != null) {
                for (Object fragment : fragments) {
                    VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;
                    if (variableDeclarationFragment.getName().toString().equals(variableName)) {
                        return fieldDeclaration;
                    }
                }
            }
        }
        return null;
    }

    public static VariableDeclarationStatement getVariableDeclarationStatementFromName(String variableName, MethodDeclaration methodDeclaration) {
        // Fetch all VariableDeclarationStatement of the method
        ArrayList<VariableDeclarationStatement> variableDeclarationStatementList = new ArrayList<>();
        methodDeclaration.accept(new VariableDeclarationStatementVisitor(variableDeclarationStatementList));
        for (VariableDeclarationStatement variableDeclarationStatement : variableDeclarationStatementList) {
            List fragments = variableDeclarationStatement.fragments();
            if (fragments != null) {
                for (Object fragment : fragments) {
                    VariableDeclarationFragment variableDeclarationFragment = (VariableDeclarationFragment) fragment;
                    if (variableDeclarationFragment.getName().toString().equals(variableName)) {
                        return variableDeclarationStatement;
                    }
                }
            }
        }
        return null;
    }

    public static ExpressionStatement getExpressionStatementFromContent(String statementContent, MethodDeclaration methodDeclaration) {
        // Fetch all ExpressionStatments
        ArrayList<ExpressionStatement> statementList = new ArrayList<>();
        methodDeclaration.accept(new ExpressionStatementVisitor(statementList));
        int i = 0;
        int statementListSize = statementList.size();
        while (i < statementListSize && !statementList.get(i).toString().equals(statementContent)) {
            i++;
        }
        if (i < statementListSize) {
            return statementList.get(i);
        } else {
            return null;
        }
    }

    public static String getCallerName(Statement statement, String methodName) {
        if (statement == null || methodName == null) {
            return null;
        } else {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) statement;
                Expression expression = expressionStatement.getExpression();
                if (expression instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation) expression;
                    // If there is an explicit caller
                    if (methodInvocation.getExpression() != null) {
                        if (methodInvocation.getName().toString().equals(methodName)) {
                            return methodInvocation.getExpression().toString();
                        }
                    }
                }
            }
            return null;
        }
    }

    public static List<Expression> getArguments(Statement statement) {
        if (statement != null) {
            if (statement instanceof ExpressionStatement) {
                ExpressionStatement expressionStatement = (ExpressionStatement) statement;
                Expression expression = expressionStatement.getExpression();
                if (expression instanceof MethodInvocation) {
                    MethodInvocation methodInvocation = (MethodInvocation) expression;
                    return (List<Expression>) methodInvocation.arguments();
                }
            }
        }
        return null;
    }
}

