package adoctorr.application.analysis;

import adoctorr.application.ast.ASTUtilities;
import adoctorr.application.bean.smell.ERBSmell;
import adoctorr.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ERBAnalyzer {

    private static final String ONCREATE_NAME = "onCreate";
    private static final String ONCREATE_TYPE = "void";
    private static final String ONCREATE_SCOPE1 = "public";
    private static final String ONCREATE_SCOPE2 = "protected";
    private static final String ONCREATE_ARGUMENT_TYPE = "Bundle";
    private static final String GPS_REQUEST_METHOD_NAME = "requestLocationUpdates";

    // Warning: Source code with method-level compile error and accents might give problems in the methodDeclaration fetch
    public ERBSmell analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile) {
        if (methodBean != null && methodDeclaration != null && compilationUnit != null && sourceFile != null) {
            // Only for public|protected void onCreate(Bundle)
            boolean onCreateFound = false;
            if (methodDeclaration.getName().toString().equals(ONCREATE_NAME)) {
                Type returnType = methodDeclaration.getReturnType2();
                if (returnType != null && returnType.toString().equals(ONCREATE_TYPE)) {
                    List modifierList = methodDeclaration.modifiers();
                    int i = 0;
                    int n = modifierList.size();
                    while (!onCreateFound && i < n) {
                        IExtendedModifier modifier = (IExtendedModifier) modifierList.get(i);
                        if (modifier.toString().equals(ONCREATE_SCOPE1) || modifier.toString().equals(ONCREATE_SCOPE2)) {
                            List parameters = methodDeclaration.parameters();
                            if (parameters != null && parameters.size() > 0) {
                                SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
                                Type parameterType = parameter.getType();
                                if (parameterType != null && parameterType.toString().equals(ONCREATE_ARGUMENT_TYPE)) {
                                    onCreateFound = true;
                                }
                            }
                        }
                        i++;
                    }
                    if (onCreateFound) {
                        // Look for the presence of the smell in the onCreate(Bundle)
                        boolean smellFound = false;
                        Block requestBlock = null;
                        Statement requestStatement = null;

                        ArrayList<Block> methodBlockList = ASTUtilities.getBlocksInMethod(methodDeclaration);
                        int k = 0;
                        while (!smellFound && k < methodBlockList.size()) {
                            Block block = methodBlockList.get(k);
                            List<Statement> statementList = (List<Statement>) block.statements();

                            int j = 0;
                            while (!smellFound && j < statementList.size()) {
                                Statement statement = statementList.get(j);
                                String callerName = ASTUtilities.getCallerName(statement, GPS_REQUEST_METHOD_NAME);
                                if (callerName != null) {
                                    FieldDeclaration fieldDeclaration = ASTUtilities.getFieldDeclarationFromName(callerName, compilationUnit);
                                    if (fieldDeclaration != null) {
                                        smellFound = true;
                                        requestBlock = block;
                                        requestStatement = statement;
                                    } else {
                                        j++;
                                    }
                                } else {
                                    j++;
                                }
                            }
                            k++;
                        }
                        if (smellFound) {
                            ERBSmell smellMethodBean = new ERBSmell();
                            smellMethodBean.setMethodBean(methodBean);
                            smellMethodBean.setResolved(false);
                            smellMethodBean.setSourceFile(sourceFile);
                            smellMethodBean.setSmellType(MethodSmell.EARLY_RESOURCE_BINDING);
                            smellMethodBean.setRequestBlock(requestBlock);
                            smellMethodBean.setRequestStatement(requestStatement);
                            return smellMethodBean;
                        }
                    }
                }
            }
        }
        return null;
    }
}
