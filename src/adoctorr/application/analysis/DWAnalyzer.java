package adoctorr.application.analysis;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.smell.DWSmell;
import adoctorr.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class DWAnalyzer extends MethodSmellAnalyzer {

    // Warning: Source code with method-level compile error and accents might give problems in the methodDeclaration fetch
    @Override
    public DWSmell analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile) {
        if (methodBean == null || methodDeclaration == null || compilationUnit == null || sourceFile == null) {
            return null;
        }
        boolean smellFound = false;
        Block acquireBlock = null;
        Statement acquireStatement = null;
        ArrayList<Block> methodBlockList = ASTUtilities.getBlocksInMethod(methodDeclaration);
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
            DWSmell smellMethodBean = new DWSmell();
            smellMethodBean.setMethodBean(methodBean);
            smellMethodBean.setSourceFile(sourceFile);
            smellMethodBean.setAcquireBlock(acquireBlock);
            smellMethodBean.setAcquireStatement(acquireStatement);
            return smellMethodBean;
        }
        return null;
    }
}
