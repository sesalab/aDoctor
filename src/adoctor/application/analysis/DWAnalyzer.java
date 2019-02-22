package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.smell.DWSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DWAnalyzer extends MethodSmellAnalyzer {

    @Override
    public DWSmell analyzeMethod(Method method) {
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
                String callerName = ASTUtilities.getCallerName(statement, DWSmell.ACQUIRE_NAME);
                if (callerName != null) {
                    // Check type of the caller
                    CompilationUnit compilationUnit = (CompilationUnit) methodDecl.getRoot();
                    FieldDeclaration fieldDeclaration = ASTUtilities.getFieldDeclarationFromName(compilationUnit, callerName);
                    VariableDeclarationStatement variableDeclarationStatement = ASTUtilities
                            .getVariableDeclarationStatementFromName(methodDecl, callerName);
                    if (fieldDeclaration != null && fieldDeclaration.getType().toString().equals(DWSmell.WAKELOCK_CLASS)
                            || variableDeclarationStatement != null && variableDeclarationStatement.getType()
                            .toString().equals(DWSmell.WAKELOCK_CLASS)) {
                        // Check if the arguments of the acquire() are absent
                        List arguments = ASTUtilities.getArguments(statement);
                        if (arguments == null || arguments.size() == 0) {
                            // Look for corresponding release
                            boolean releaseFound = false;
                            for (int j = i + 1; j < statements.size() && !releaseFound; j++) {
                                Statement statement2 = statements.get(j);
                                String callerName2 = ASTUtilities.getCallerName(statement2, DWSmell.RELEASE_NAME);
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
    }
}
