package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.smell.DWSmell;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DWAnalyzer extends MethodSmellAnalyzer {

    // Warning: Source code with method-level compile error and accents might give problems in the methodDeclaration fetch
    @Override
    public DWSmell analyzeMethod(Method method) throws IOException {
        if (method == null) {
            return null;
        }
        File sourceFile = method.getSourceFile();
        if (sourceFile == null) {
            return null;
        }
        CompilationUnit compilationUnit = ASTUtilities.getCompilationUnit(sourceFile);
        if (compilationUnit == null) {
            return null;
        }
        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(method.getLegacyMethodBean().getTextContent(), compilationUnit);
        if (methodDeclaration == null) {
            return null;
        }

        boolean smellFound = false;
        Block acquireBlock = null;
        Statement acquireStatement = null;
        ArrayList<Block> methodBlockList = ASTUtilities.getBlocks(methodDeclaration);
        // Look for the block with acquire() but not the release()
        for (int k = 0; k < methodBlockList.size() && !smellFound; k++) {
            Block block = methodBlockList.get(k);
            List<Statement> statementList = (List<Statement>) block.statements();
            for (int i = 0; i < statementList.size(); i++) {
                Statement statement = statementList.get(i);
                String callerName = ASTUtilities.getCallerName(statement, DWSmell.ACQUIRE_NAME);
                if (callerName != null) {
                    // Check type of the caller
                    FieldDeclaration fieldDeclaration = ASTUtilities.getFieldDeclarationFromName(callerName, compilationUnit);
                    VariableDeclarationStatement variableDeclarationStatement = ASTUtilities
                            .getVariableDeclarationStatementFromName(callerName, methodDeclaration);
                    if (fieldDeclaration != null && fieldDeclaration.getType().toString().equals(DWSmell.WAKELOCK_CLASS)
                            || variableDeclarationStatement != null && variableDeclarationStatement.getType()
                            .toString().equals(DWSmell.WAKELOCK_CLASS)) {
                        // Check if the arguments of the acquire() are absent
                        List arguments = ASTUtilities.getArguments(statement);
                        if (arguments == null || arguments.size() == 0) {
                            // Look for corresponding release
                            boolean releaseFound = false;
                            for (int j = i + 1; j < statementList.size() && !releaseFound; j++) {
                                Statement statement2 = statementList.get(j);
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
