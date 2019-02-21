package adoctor.application.analysis;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.Method;
import adoctor.application.bean.smell.ERBSmell;
import org.eclipse.jdt.core.dom.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class ERBAnalyzer extends MethodSmellAnalyzer {

    // Warning: Source code with method-level compile error and accents might give problems in the methodDeclaration fetch
    @Override
    public ERBSmell analyzeMethod(Method method) throws IOException {
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
        MethodDeclaration methodDeclaration = ASTUtilities.getMethodDeclarationFromContent(compilationUnit, method.getLegacyMethodBean().getTextContent());
        if (methodDeclaration == null) {
            return null;
        }

        // Only for public|protected void onCreate(Bundle)
        boolean onCreateFound = false;
        if (methodDeclaration.getName().toString().equals(ERBSmell.ONCREATE_NAME)) {
            Type returnType = methodDeclaration.getReturnType2();
            if (returnType != null && returnType.toString().equals(ERBSmell.ONCREATE_TYPE)) {
                List modifierList = methodDeclaration.modifiers();
                for (int i = 0; i < modifierList.size() && !onCreateFound; i++) {
                    IExtendedModifier modifier = (IExtendedModifier) modifierList.get(i);
                    if (modifier.toString().equals(ERBSmell.ONCREATE_SCOPE1) || modifier.toString().equals(ERBSmell.ONCREATE_SCOPE2)) {
                        List parameters = methodDeclaration.parameters();
                        if (parameters != null && parameters.size() > 0) {
                            SingleVariableDeclaration parameter = (SingleVariableDeclaration) parameters.get(0);
                            Type parameterType = parameter.getType();
                            if (parameterType != null && parameterType.toString().equals(ERBSmell.ONCREATE_ARGUMENT_TYPE)) {
                                onCreateFound = true;
                            }
                        }
                    }
                }
                if (onCreateFound) {
                    // Look for the presence of the smell in the onCreate(Bundle)
                    boolean smellFound = false;
                    Block requestBlock = null;
                    Statement requestStatement = null;
                    ArrayList<Block> methodBlockList = ASTUtilities.getBlocks(methodDeclaration);
                    for (int j = 0; j < methodBlockList.size() && !smellFound; j++) {
                        Block block = methodBlockList.get(j);
                        List<Statement> statementList = (List<Statement>) block.statements();
                        for (int k = 0; k < statementList.size() && !smellFound; k++) {
                            Statement statement = statementList.get(k);
                            String callerName = ASTUtilities.getCallerName(statement, ERBSmell.GPS_REQUEST_METHOD_NAME);
                            if (callerName != null) {
                                FieldDeclaration fieldDeclaration = ASTUtilities.getFieldDeclarationFromName(compilationUnit, callerName);
                                if (fieldDeclaration != null) {
                                    smellFound = true;
                                    requestBlock = block;
                                    requestStatement = statement;
                                }
                            }
                        }
                    }
                    if (smellFound) {
                        ERBSmell smell = new ERBSmell();
                        smell.setMethod(method);
                        smell.setRequestBlock(requestBlock);
                        smell.setRequestStatement(requestStatement);
                        return smell;
                    }
                }
            }
        }
        return null;
    }
}
