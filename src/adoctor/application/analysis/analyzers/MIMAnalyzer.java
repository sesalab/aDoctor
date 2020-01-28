package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.MIMSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public class MIMAnalyzer extends ClassSmellAnalyzer {
    private static final String OVERRIDE = "Override";

    //private List<SimpleName> internalMethods;
    private List<SimpleName> internalFields;

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

        // Fetch all instance variable names
        internalFields = new ArrayList<>();
        FieldDeclaration[] fieldDecls = typeDecl.getFields();
        for (FieldDeclaration fieldDecl : fieldDecls) {
            if (!Modifier.isStatic(fieldDecl.getModifiers())) {
                List<VariableDeclarationFragment> varFrags = (List<VariableDeclarationFragment>) fieldDecl.fragments();
                for (VariableDeclarationFragment varFrag : varFrags) {
                    List<SimpleName> varNames = ASTUtilities.getSimpleNames(varFrag);
                    if (varNames != null) {
                        internalFields.addAll(varNames);
                    }
                }
            }
        }

        // For each method declaration inside the target class
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
        List<ThisExpression> thisExpressions = ASTUtilities.getThisExpressions(methodDecl);
        List<SuperMethodInvocation> superMethodInvocations = ASTUtilities.getSuperMethodInvocations(methodDecl);
        List<SuperFieldAccess> superFieldAccess = ASTUtilities.getSuperFieldAccess(methodDecl);
        boolean isNotConstructor = !methodDecl.isConstructor();
        boolean hasNonEmptyBody = methodDecl.getBody() != null && methodDecl.getBody().statements().size() > 0;
        boolean isNonStatic = !Modifier.isStatic(methodDecl.getModifiers());
        boolean doesNotUseThis = thisExpressions == null || thisExpressions.size() == 0;
        boolean doesNotHaveSuperMethodInvocation = superMethodInvocations == null || superMethodInvocations.size() == 0;
        boolean doesNotHaveSuperFieldAccess = superFieldAccess == null || superFieldAccess.size() == 0;
        boolean doesNotUseInternalFields = !useInternalFields(methodDecl);
        boolean doesNotHaveOverride = !hasOverrideAnnotation(methodDecl);
        boolean doesNotDoInternalCalls = !doesInternalCall(methodDecl);
        return isNotConstructor &&
                hasNonEmptyBody &&
                isNonStatic &&
                doesNotUseThis &&
                doesNotHaveSuperMethodInvocation &&
                doesNotHaveSuperFieldAccess &&
                doesNotUseInternalFields &&
                doesNotHaveOverride &&
                doesNotDoInternalCalls;
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
                    /*String methodName = methodInvocation.getName().getIdentifier();*/
                    return true;
                }
            }
        }
        return false;
    }

    private boolean useInternalFields(MethodDeclaration methodDecl) {
        List<SimpleName> localNames = new ArrayList<>();
        List<SimpleName> names = ASTUtilities.getSimpleNames(methodDecl);
        if (names != null) {
            for (SimpleName name : names) {
                if (name.isDeclaration()) {
                    localNames.add(name);
                } else {
                    String nameId = name.getIdentifier();
                    // Check if the referenced name IS NOT a local variable
                    if (localNames.stream().noneMatch(dName -> dName.getIdentifier().equals(nameId))) {
                        // Check if the referenced name IS of an internal field
                        if (internalFields.stream().anyMatch(iField -> iField.getIdentifier().equals(nameId))) {
                            return true;
                        }
                        // TODO: Recursively call all superclasses and accumulate superFields and superMethods
                        Optional<ITypeBinding> superclass = Optional.ofNullable(methodDecl.resolveBinding())
                                .map(IMethodBinding::getDeclaringClass)
                                .map(ITypeBinding::getSuperclass);
                        if (superclass.isPresent()) {
                            //TODO: Check if it is "Object", that is the base case
                            IVariableBinding[] superFields = superclass.get().getDeclaredFields();
                            // Check if the referenced name IS of an inherited field
                            if (Arrays.stream(superFields).anyMatch(sField -> sField.getName().equals(nameId))) {
                                System.out.println("Escluso perché usa un campo ereditato");
                                return true;
                            }
                            String methodName = methodDecl.getName().getIdentifier();
                            IMethodBinding[] superMethods = superclass.get().getDeclaredMethods();
                            System.out.println("Metodi del padre");
                            Arrays.stream(superMethods).map(IMethodBinding::getName).forEach(System.out::println);
                            if (Arrays.stream(superMethods).anyMatch(sMethod -> sMethod.getName().equals(methodName))) {
                                System.out.println("Escluso perché il metodo è un override");
                                return true;
                            }
                            if (Arrays.stream(superMethods).anyMatch(sMethod -> sMethod.getName().equals(nameId))) {
                                System.out.println("Escluso perché usa un metodo ereditato");
                                return true;
                            }
                        } else {
                            System.out.println("Superclass non c'è!");
                        }
                    }
                }
            }
        }
        return false;
    }

    /*
    private IVariableBinding[] getAllInheritedFields(MethodDeclaration methodDecl) {

    }
     */
}
