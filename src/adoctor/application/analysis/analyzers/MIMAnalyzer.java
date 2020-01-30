package adoctor.application.analysis.analyzers;

import adoctor.application.ast.ASTUtilities;
import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;
import adoctor.application.smell.MIMSmell;
import org.eclipse.jdt.core.dom.*;

import java.util.*;

public class MIMAnalyzer extends ClassSmellAnalyzer {
    private static final String OVERRIDE = "Override";
    private static final String OBJECT = "Object";

    private List<SimpleName> internalFields;
    private IVariableBinding[] superFields;
    private IMethodBinding[] superMethods;

    private static boolean useInternalProperties(MethodDeclaration methodDecl, List<SimpleName> internalFields,
                                                 IVariableBinding[] superFields, IMethodBinding[] superMethods) {
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
                        boolean isInternalField = internalFields.stream().anyMatch(iField -> iField.getIdentifier().equals(nameId));
                        boolean isSuperField = Arrays.stream(superFields).anyMatch(sField -> sField.getName().equals(nameId));
                        boolean isSuperMethod = Arrays.stream(superMethods).anyMatch(sMethod -> sMethod.getName().equals(nameId));
                        // Check if the referenced name IS of an internal|inherited field or a call to an inherited method
                        if (isInternalField || isSuperField || isSuperMethod) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static boolean isOverride(MethodDeclaration methodDecl, IMethodBinding[] superMethods) {
        String methodName = methodDecl.getName().getIdentifier();
        // Check if the analyzed method IS an overriden method
        return Arrays.stream(superMethods).anyMatch(sMethod -> sMethod.getName().equals(methodName));
    }

    private static boolean doesInternalCall(MethodDeclaration methodDecl) {
        List<MethodInvocation> methodInvocations = ASTUtilities.getMethodInvocations(methodDecl);
        if (methodInvocations != null) {
            for (MethodInvocation methodInvocation : methodInvocations) {
                if (methodInvocation.getExpression() == null) {
                    return true;
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

    // Iterative call to get all super fields
    private static IVariableBinding[] getAllSuperFields(ITypeBinding iTypeBinding) {
        Set<IVariableBinding> allSuperFields = new HashSet<>();
        Optional<ITypeBinding> superclass = Optional.ofNullable(iTypeBinding.getSuperclass());
        while (superclass.isPresent() && !superclass.get().getName().equals(OBJECT)) {
            allSuperFields.addAll(Arrays.asList(superclass.get().getDeclaredFields()));
            superclass = Optional.ofNullable(superclass.get().getSuperclass());
        }
        return allSuperFields.toArray(new IVariableBinding[0]);
    }

    // Iterative call to get all super fields
    private static IMethodBinding[] getAllSuperMethods(ITypeBinding iTypeBinding) {
        Set<IMethodBinding> allSuperMethods = new HashSet<>();
        Optional<ITypeBinding> superclass = Optional.ofNullable(iTypeBinding.getSuperclass());
        while (superclass.isPresent() && !superclass.get().getName().equals(OBJECT)) {
            allSuperMethods.addAll(Arrays.asList(superclass.get().getDeclaredMethods()));
            superclass = Optional.ofNullable(superclass.get().getSuperclass());
        }
        return allSuperMethods.toArray(new IMethodBinding[0]);
    }

    //TODO: Change these two methods into a single one because they are basically the same

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

        ITypeBinding iTypeBinding = typeDecl.resolveBinding();
        // Fetch all super fields
        superFields = getAllSuperFields(iTypeBinding);
        // Arrays.stream(superFields).map(IVariableBinding::getName).forEach(System.out::println);

        // Fetch all super methods
        superMethods = getAllSuperMethods(iTypeBinding);
        // Arrays.stream(superMethods).map(IMethodBinding::getName).forEach(System.out::println);

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
        boolean doesNotUseInternalProperties = !useInternalProperties(methodDecl, this.internalFields,
                this.superFields, this.superMethods);
        boolean doesNotHaveOverride = !hasOverrideAnnotation(methodDecl);
        boolean isNotOverride = !isOverride(methodDecl, this.superMethods);
        boolean doesNotDoInternalCalls = !doesInternalCall(methodDecl);
        return isNotConstructor &&
                hasNonEmptyBody &&
                isNonStatic &&
                doesNotUseThis &&
                doesNotHaveSuperMethodInvocation &&
                doesNotHaveSuperFieldAccess &&
                doesNotUseInternalProperties &&
                doesNotHaveOverride &&
                isNotOverride &&
                doesNotDoInternalCalls;
    }
}
