package adoctor.application.bean.smell;

import beans.MethodBean;

import java.io.File;

public abstract class MethodSmell extends Smell {

    private MethodBean methodBean;
    private String smellName;
    private String smellDescription;
    private File sourceFile;

    public MethodSmell() {
        methodBean = null;
        smellName = "";
        smellDescription = "";
        sourceFile = null;
    }

    public MethodBean getMethodBean() {
        return methodBean;
    }

    public void setMethodBean(MethodBean methodBean) {
        this.methodBean = methodBean;
    }

    public String getSmellName() {
        return smellName;
    }

    public void setSmellName(String smellName) {
        this.smellName = smellName;
    }

    public String getSmellDescription() {
        return smellDescription;
    }

    public void setSmellDescription(String smellDescription) {
        this.smellDescription = smellDescription;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
}