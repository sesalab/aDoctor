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
    private List<SimpleName> internalMethods;
    private List<SimpleName> internalVars;

    @Override
    public ClassSmell analyze(ClassBean classBean) {
        if (classBean == null) {
            return null;
        }
        TypeDeclaration typeDecl = classBean.getTypeDeclaration();

        // Instance methods names
        internalMethods = new ArrayList<>();
        MethodDeclaration[] methodDecls = typeDecl.getMethods();
        for (MethodDeclaration methodDecl : methodDecls) {
            if (!Modifier.isStatic(methodDecl.getModifiers())) {
                internalMethods.add(methodDecl.getName());
            }
        }

        // Instance variable names
        internalVars = new ArrayList<>();
        FieldDeclaration[] fieldDecls = typeDecl.getFields();
        for (FieldDeclaration fieldDecl : fieldDecls) {
            List<SimpleName> fieldNames = ASTUtilities.getSimpleNames(fieldDecl);
            if (fieldNames != null) {
                internalVars.addAll(fieldNames);
            }
        }

        for (MethodDeclaration methodDecl : methodDecls) {
            if (hasMIM(methodDecl)) {
                MIMSmell smell = new MIMSmell();
                smell.setSmellyMethod(methodDecl);
                smell.setClassBean(classBean);
                System.out.println("Smell trovato nella classe " + typeDecl.getName() + ", nel metodo: " + methodDecl.getName());
                return smell;
            }
        }
        return null;
    }

    private boolean hasMIM(MethodDeclaration methodDecl) {
        // If the method is a constructor, MIM is surely absent
        if (!methodDecl.isConstructor()) {
            // If the method body is empty, we want to ignore it
            if (methodDecl.getBody().statements().size() > 0) {
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

    private boolean hasOverrideAnnotation(MethodDeclaration methodDecl) {
        List<IExtendedModifier> modifiers = (List<IExtendedModifier>) methodDecl.modifiers();
        for (IExtendedModifier modifier : modifiers) {
            if (modifier.isAnnotation()) {
                Annotation annotation = (Annotation) modifier;
                if (annotation.getTypeName().toString().equals("Override")) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean doesInternalCall(MethodDeclaration methodDecl) {
        List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(methodDecl);
        if (methodInvocations != null) {
            for (MethodInvocation methodInvocation : methodInvocations) {
                if (methodInvocation.getExpression() == null) {
                    String methodName = methodInvocation.getName().getIdentifier();
                    //TODO Low Try to use the bindings to enable a finer check: if the called method belongs to a superclass
                    System.out.println("\tL'uso di " + methodName + " rende il metodo NON MIM");
                    /*IMethodBinding iMethodBinding = methodDecl.resolveBinding();
                    System.out.println(iMethodBinding.getDeclaringClass().getBinaryName());*/
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
                    if (!declaredNames.contains(name) && internalVars.contains(name)) {
                        System.out.println("L'uso di " + name + " rende il metodo NON MIM");
                        return true;
                    }
                }

            }
        }
        return false;
    }
}
