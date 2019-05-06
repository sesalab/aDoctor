package adoctor.application.bean;

import org.eclipse.jdt.core.dom.TypeDeclaration;

import java.io.File;

public class ClassBean extends Bean {
    private File sourceFile;
    private TypeDeclaration typeDeclaration;
    private beans.ClassBean legacyClassBean;

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }

    public TypeDeclaration getTypeDeclaration() {
        return typeDeclaration;
    }

    public void setTypeDeclaration(TypeDeclaration typeDeclaration) {
        this.typeDeclaration = typeDeclaration;
    }

    public beans.ClassBean getLegacyClassBean() {
        return legacyClassBean;
    }

    public void setLegacyClassBean(beans.ClassBean legacyClassBean) {
        this.legacyClassBean = legacyClassBean;
    }

}
