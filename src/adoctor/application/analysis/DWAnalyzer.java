package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.smell.DWSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.List;

@SuppressWarnings("unchecked")
public class DWAnalyzer extends MethodSmellAnalyzer {
    private static final String ACQUIRE = "acquire";
    private static final String RELEASE = "release";
    private static final String POWER_MANAGER_WAKELOCK = "PowerManager.WakeLock";

    @Override
    public DWSmell analyzeMethod(Method method) {
        if (method == null) {
            return null;
        }
        MethodDeclaration methodDecl = method.getMethodDecl();
        if (methodDecl == null) {
            return null;
        }

        Block methodBlock = methodDecl.getBody();
        List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(methodBlock);
        if (methodInvocations == null) {
            return null;
        }
        for (int i = 0; i < methodInvocations.size(); i++) {
            MethodInvocation iMethodInvocation = methodInvocations.get(i);
            if (iMethodInvocation.getName().getIdentifier().equals(ACQUIRE) && iMethodInvocation.arguments().size() == 0) {
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
                    smell.setMethod(method);

                    //TODO Cambiare interfaccia di DWSmell. Soluzione temporanea per retrocompatibilità
                    ASTNode smellyBlock = iMethodInvocation;
                    while (!(smellyBlock instanceof Block)) {
                        smellyBlock = smellyBlock.getParent();
                    }
                    smell.setAcquireBlock((Block) smellyBlock);

                    ASTNode smellyStatement = iMethodInvocation;
                    while (!(smellyStatement instanceof Statement)) {
                        smellyStatement = smellyStatement.getParent();
                    }
                    smell.setAcquireBlock((Block) smellyBlock);
                    smell.setAcquireStatement((Statement) smellyStatement);

                    return smell;
                }
            }
        }
        return null;
    }

    /*
    if (method == null) {
            return null;
        }
        MethodDeclaration methodDecl = method.getMethodDecl();
        if (methodDecl == null) {
            return null;
        }

        boolean smellFound = false;
        Block acquireBlock = null;
        Statement acquireStatement = null;
        ArrayList<Block> blocks = ASTUtilities.getBlocks(methodDecl);
        // Look for the block with acquire() but not the release()
        for (int k = 0; k < blocks.size() && !smellFound; k++) {
            Block block = blocks.get(k);
            List<Statement> statements = (List<Statement>) block.statements();
            for (int i = 0; i < statements.size(); i++) {
                Statement statement = statements.get(i);
                String callerName = ASTUtilities.getCallerName(statement, ACQUIRE);
                if (callerName != null) {
                    // Check type of the caller
                    CompilationUnit compilationUnit = (CompilationUnit) methodDecl.getRoot();
                    FieldDeclaration fieldDeclaration = ASTUtilities.getFieldDeclarationFromName(compilationUnit, callerName);
                    VariableDeclarationStatement variableDeclarationStatement = ASTUtilities
                            .getVariableDeclarationStatementFromName(methodDecl, callerName);
                    if (fieldDeclaration != null && fieldDeclaration.getType().toString().equals(POWER_MANAGER_WAKELOCK)
                            || variableDeclarationStatement != null && variableDeclarationStatement.getType()
                            .toString().equals(POWER_MANAGER_WAKELOCK)) {
                        // Check if the arguments of the acquire() are absent
                        List arguments = ASTUtilities.getArguments(statement);
                        if (arguments == null || arguments.size() == 0) {
                            // Look for corresponding release
                            boolean releaseFound = false;
                            for (int j = i + 1; j < statements.size() && !releaseFound; j++) {
                                Statement statement2 = statements.get(j);
                                String callerName2 = ASTUtilities.getCallerName(statement2, RELEASE);
                                if (callerName.equals(callerName2)) {
                                    releaseFound = true;
                                }
                            }
                            if (!releaseFound) {
                                smellFound = true;
                                acquireBlock = block;
                                acquireStatement = statement;
                            }
                        }
                    }
                }
            }
        }
        if (smellFound) {
            DWSmell smell = new DWSmell();
            smell.setMethod(method);
            smell.setAcquireBlock(acquireBlock);
            smell.setAcquireStatement(acquireStatement);
            return smell;
        }
        return null;

    public static String getCallerName(Statement statement, String methodName) {
        if (statement == null || methodName == null) {
            return null;
        }
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
     */
}