package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.DWSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

public class DWAnalyzer extends ClassSmellAnalyzer {
    private static final String ACQUIRE = "acquire";
    private static final String RELEASE = "release";
    private static final String POWER_MANAGER_WAKELOCK = "PowerManager.WakeLock";

    @Override
    public DWSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        MethodDeclaration[] methods = classBean.getTypeDeclaration().getMethods();
        for (MethodDeclaration methodDecl : methods) {
            List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(methodDecl);
            if (methodInvocations == null) {
                return null;
            }
            for (int i = 0; i < methodInvocations.size(); i++) {
                MethodInvocation iMethodInvocation = methodInvocations.get(i);
                if (iMethodInvocation.getName().getIdentifier().equals(ACQUIRE) && iMethodInvocation.arguments().size() == 0) {
                    // Check if this variable has PowerManager.Wakelock type
                    if (!hasPowerManagerWakelockType(iMethodInvocation.getExpression())) {
                        return null;
                    }
                    String acquireCaller = iMethodInvocation.getExpression().toString();
                    boolean found = false;
                    for (int j = i + 1; j < methodInvocations.size() && !found; j++) {
                        MethodInvocation jMethodInvocation = methodInvocations.get(j);
                        if (jMethodInvocation.getName().getIdentifier().equals(RELEASE)
                                && jMethodInvocation.getExpression().toString().equals(acquireCaller)) {
                            found = true;
                        }
                    }
                    if (!found) {
                        DWSmell smell = new DWSmell();
                        smell.setClassBean(classBean);
                        smell.setAcquireExpression(iMethodInvocation.getExpression());
                        return smell;
                    }
                }
            }
        }
        return null;
    }

    private boolean hasPowerManagerWakelockType(Expression expression) {
        List<FieldDeclaration> fieldDecls = ASTUtilities.getFieldDeclarations(expression.getRoot());
        List<VariableDeclarationStatement> varDecls = ASTUtilities.getVariableDeclarationStatements(expression.getRoot());

        if (fieldDecls == null || varDecls == null) {
            return false;
        }
        for (FieldDeclaration fieldDecl : fieldDecls) {
            if (fieldDecl.getType().toString().equals(POWER_MANAGER_WAKELOCK)) {
                return true;
            }
        }
        for (VariableDeclarationStatement varDecl : varDecls) {
            if (varDecl.getType().toString().equals(POWER_MANAGER_WAKELOCK)) {
                return true;
            }
        }
        return false;
    }
}