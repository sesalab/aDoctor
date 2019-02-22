package adoctor.application.bean;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class Method extends Bean {
    private MethodDeclaration methodDecl;
    private beans.MethodBean legacyMethodBean;

    public MethodDeclaration getMethodDecl() {
        return methodDecl;
    }

    public void setMethodDecl(MethodDeclaration methodDecl) {
        this.methodDecl = methodDecl;
    }

    public beans.MethodBean getLegacyMethodBean() {
        return legacyMethodBean;
    }

    public void setLegacyMethodBean(beans.MethodBean legacyMethodBean) {
        this.legacyMethodBean = legacyMethodBean;
    }
}
