package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ERBSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class ERBAnalyzer extends ClassSmellAnalyzer {
    private static final String ONCREATE_NAME = "onCreate";
    private static final String ONCREATE_TYPE = "void";
    private static final String ONCREATE_ARGUMENT_TYPE = "Bundle";
    private static final String GPS_REQUEST_METHOD_NAME = "requestLocationUpdates";

    private static MethodDeclaration getOnCreate(ClassBean classBean) {
        MethodDeclaration[] methods = classBean.getTypeDeclaration().getMethods();
        for (MethodDeclaration methodDecl : methods) {
            if (methodDecl.getName().toString().equals(ONCREATE_NAME)) {
                Type returnType = methodDecl.getReturnType2();
                if (returnType != null && returnType.toString().equals(ONCREATE_TYPE)) {
                    if (Modifier.isPublic(methodDecl.getModifiers()) || Modifier.isProtected(methodDecl.getModifiers())) {
                        List parameters = methodDecl.parameters();
                        if (parameters != null && parameters.size() > 0) {
                            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
                            Type parameterType = parameter.getType();
                            if (parameterType != null && parameterType.toString().equals(ONCREATE_ARGUMENT_TYPE)) {
                                return methodDecl;
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static Statement getBindingStatement(MethodDeclaration methodDecl) {
        List<Block> blocks = ASTUtilities.getBlocks(methodDecl);
        for (Block block : blocks) {
            List<Statement> statements = (List<Statement>) block.statements();
            for (Statement statement : statements) {
                Expression caller = getCaller(statement);
                if (caller != null) {
                    if (hasField(methodDecl, caller.toString())) {
                        return statement;
                    }
                }
            }
        }
        return null;
    }

    private static Expression getCaller(Statement statement) {
        if (statement instanceof ExpressionStatement) {
            Expression expr = ((ExpressionStatement) statement).getExpression();
            if (expr instanceof MethodInvocation) {
                MethodInvocation methodInvocation = (MethodInvocation) expr;
                // If there is an explicit caller
                if (methodInvocation.getName().toString().equals(GPS_REQUEST_METHOD_NAME)) {
                    return methodInvocation.getExpression();
                }
            }
        }
        return null;
    }

    private static boolean hasField(MethodDeclaration methodDecl, String callerName) {
        FieldDeclaration[] fieldDecls = ((TypeDeclaration) methodDecl.getParent()).getFields();
        for (FieldDeclaration fieldDecl : fieldDecls) {
            List<VariableDeclarationFragment> varFrags = (List<VariableDeclarationFragment>) fieldDecl.fragments();
            for (VariableDeclarationFragment varFrag : varFrags) {
                if (varFrag.getName().toString().equals(callerName)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public ERBSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        MethodDeclaration onCreateDecl = getOnCreate(classBean);
        if (onCreateDecl == null) {
            return null;
        }
        Statement bindingStatement = getBindingStatement(onCreateDecl);
        if (bindingStatement == null) {
            return null;
        }
        ERBSmell smell = new ERBSmell();
        smell.setClassBean(classBean);
        smell.setRequestStatement(bindingStatement);
        smell.setOnCreate(onCreateDecl);
        return smell;
    }
}
