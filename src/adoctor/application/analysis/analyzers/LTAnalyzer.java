package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.LTSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class LTAnalyzer extends ClassSmellAnalyzer {
    private static final String ACTIVITY = "Activity";
    private static final String APP_COMPACT_ACTIVITY = "AppCompactActivity";
    private static final String THREAD = "Thread";
    private static final String START = "start";
    private static final String STOP = "stop";

    @Override
    public ClassSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        TypeDeclaration typeDecl = classBean.getTypeDeclaration();
        Type superClassType = typeDecl.getSuperclassType();
        if (superClassType == null) {
            return null;
        }
        // Only for activity subclasses
        String superclassType = superClassType.toString();
        if (superclassType.equals(ACTIVITY) || superclassType.equals(APP_COMPACT_ACTIVITY)) {
            List<FieldDeclaration> fieldDecls = ASTUtilities.getFieldDeclarations(typeDecl);
            // Without instance variables we have nothing to do
            if (fieldDecls == null) {
                return null;
            }

            // Only Thread instance variables
            List<VariableDeclarationFragment> threadInstanceVars = new ArrayList<>();
            for (FieldDeclaration fieldDecl : fieldDecls) {
                if (fieldDecl.getType().toString().equals(THREAD)) {
                    threadInstanceVars.addAll(fieldDecl.fragments());
                }
            }
            if (threadInstanceVars.size() == 0) {
                return null;
            }

            // We need Class Instance Creations and all method invocations
            List<ClassInstanceCreation> classInstanceCreations = ASTUtilities.getClassInstanceCreations(typeDecl);
            if (classInstanceCreations == null) {
                return null;
            }
            List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(typeDecl);
            if (methodInvocations == null) {
                return null;
            }

            for (VariableDeclarationFragment threadInstanceVar : threadInstanceVars) {
                // Check if there is a ClassInstanceCreation of that Thread
                if (isCreated(threadInstanceVar, classInstanceCreations)) {
                    // Not so fine grained, but it's a start: check the presence of start() and the absence of stop()
                    if (isStartedButNotStopped(threadInstanceVar, methodInvocations)) {
                        LTSmell ltSmell = new LTSmell();
                        ltSmell.setClassBean(classBean);
                        ltSmell.setSmellyVariableDeclarationFragment(threadInstanceVar);
                        return ltSmell;
                    }
                }
            }
        }
        return null;
    }

    private static boolean isStartedButNotStopped(VariableDeclarationFragment threadVar, List<MethodInvocation> methodInvocations) {
        List<MethodInvocation> threadInvocations = new ArrayList<>();
        for (MethodInvocation methodInvocation : methodInvocations) {
            Expression invocationExpr = methodInvocation.getExpression();
            if (invocationExpr != null && invocationExpr.toString().equals(threadVar.toString())) {
                threadInvocations.add(methodInvocation);
            }
        }
        boolean foundStart = false, foundStop = false;
        for (MethodInvocation threadInvocation : threadInvocations) {
            SimpleName invocationName = threadInvocation.getName();
            foundStart = foundStart || invocationName.getIdentifier().equals(START);
            foundStop = foundStop || invocationName.getIdentifier().equals(STOP);
        }
        return foundStart && !foundStop;
    }

    private static boolean isCreated(VariableDeclarationFragment threadVar, List<ClassInstanceCreation> classInstanceCreations) {
        for (ClassInstanceCreation classInstanceCreation : classInstanceCreations) {
            if (classInstanceCreation.getType().toString().equals(THREAD)) {
                if (classInstanceCreation.getParent() instanceof Assignment) {
                    Assignment assignment = (Assignment) classInstanceCreation.getParent();
                    if (assignment.getLeftHandSide().toString().equals(threadVar.toString())) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
