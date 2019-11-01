package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.MIMSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("unchecked")
public class MIMAnalyzer extends ClassSmellAnalyzer {
    private static final String OVERRIDE = "Override";

    //private List<SimpleName> internalMethods;
    private List<SimpleName> internalVars;

    @Override
    public ClassSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        TypeDeclaration typeDecl = classBean.getTypeDeclaration();
        MethodDeclaration[] methodDecls = typeDecl.getMethods();

        /*
        // Instance methods names
        internalMethods = new ArrayList<>();
        for (MethodDeclaration methodDecl : methodDecls) {
            if (!Modifier.isStatic(methodDecl.getModifiers())) {
                internalMethods.add(methodDecl.getName());
            }
        }
        */

        // Instance variable names
        internalVars = new ArrayList<>();
        FieldDeclaration[] fieldDecls = typeDecl.getFields();
        for (FieldDeclaration fieldDecl : fieldDecls) {
            if (!Modifier.isStatic(fieldDecl.getModifiers())) {
                List<VariableDeclarationFragment> varFrags = (List<VariableDeclarationFragment>) fieldDecl.fragments();
                for (VariableDeclarationFragment varFrag : varFrags) {
                    List<SimpleName> varNames = ASTUtilities.getSimpleNames(varFrag);
                    if (varNames != null) {
                        internalVars.addAll(varNames);
                    }
                }
            }
        }

        for (MethodDeclaration methodDecl : methodDecls) {
            if (hasMIM(methodDecl)) {
                MIMSmell smell = new MIMSmell();
                smell.setSmellyMethod(methodDecl);
                smell.setClassBean(classBean);
                return smell;
            }
        }
        return null;
    }

    private boolean hasMIM(MethodDeclaration methodDecl) {
        // If the method is a constructor, MIM is surely absent
        if (!methodDecl.isConstructor()) {
            // If the method body is empty, we want to ignore it
            if (methodDecl.getBody() != null && methodDecl.getBody().statements().size() > 0) {
                // If the method is static, MIM si surely absent (trivially)
                if (!Modifier.isStatic(methodDecl.getModifiers())) {
                    // If there is a this expression, MIM is surely absent
                    List<ThisExpression> thisExpressions = ASTUtilities.getThisExpressions(methodDecl);
                    if (thisExpressions == null || thisExpressions.size() == 0) {
                        // If there is a super expression, MIM is surely absent
                        List<SuperMethodInvocation> superMethodInvocations = ASTUtilities.getSuperMethodInvocations(methodDecl);
                        if (superMethodInvocations == null || superMethodInvocations.size() == 0) {
                            List<SuperFieldAccess> superFieldAccess = ASTUtilities.getSuperFieldAccess(methodDecl);
                            if (superFieldAccess == null || superFieldAccess.size() == 0) {
                                // If it has an override annotation, MIM is surely absent
                                if (!hasOverrideAnnotation(methodDecl)) {
                                    // If there is an internal method call, MIM is surely absent
                                    if (!doesInternalCall(methodDecl)) {
                                        // If there is a referenced instance variable not shadowed by a local one, MIM is surely absent
                                        return !useInternalVar(methodDecl);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean hasOverrideAnnotation(MethodDeclaration methodDecl) {
        List<IExtendedModifier> modifiers = (List<IExtendedModifier>) methodDecl.modifiers();
        for (IExtendedModifier modifier : modifiers) {
            if (modifier.isAnnotation()) {
                Annotation annotation = (Annotation) modifier;
                if (annotation.getTypeName().toString().equals(OVERRIDE)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean doesInternalCall(MethodDeclaration methodDecl) {
        List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(methodDecl);
        if (methodInvocations != null) {
            for (MethodInvocation methodInvocation : methodInvocations) {
                if (methodInvocation.getExpression() == null) {
                    //TODO Low Try to use the bindings to enable a finer check: if the called method belongs to a superclass
                    /*String methodName = methodInvocation.getName().getIdentifier();*/
                    return true;
                }
            }
        }
        return false;
    }

    private boolean useInternalVar(MethodDeclaration methodDecl) {
        List<SimpleName> declaredNames = new ArrayList<>();
        List<SimpleName> totalNames = ASTUtilities.getSimpleNames(methodDecl);
        if (totalNames != null) {
            for (SimpleName name : totalNames) {
                if (name.isDeclaration()) {
                    declaredNames.add(name);
                } else {
                    boolean found = false;
                    for (int i = 0; i < declaredNames.size() && !found; i++) {
                        SimpleName declaredName = declaredNames.get(i);
                        found = declaredName.getIdentifier().equals(name.getIdentifier());
                    }
                    if (!found) {
                        for (int i = 0; i < internalVars.size() && !found; i++) {
                            SimpleName internalVar = internalVars.get(i);
                            found = internalVar.getIdentifier().equals(name.getIdentifier());
                        }
                        if (found) {
                            return true;
                        }
                    }
                }

            }
        }
        return false;
    }
}
