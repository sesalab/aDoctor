package adoctorr.application.bean.smell;

import beans.MethodBean;

import java.io.File;

public abstract class MethodSmell extends Smell {
    public static final int DURABLE_WAKELOCK = 1;
    public static final int EARLY_RESOURCE_BINDING = 4;

    private MethodBean methodBean;
    private int smellType;
    private boolean resolved;
    private File sourceFile;

    public MethodSmell() {
    }

    public static String getSmellName(int smellType) {
        switch (smellType) {
            case DURABLE_WAKELOCK:
                return "Durable Wakelock";
            case EARLY_RESOURCE_BINDING:
                return "Early Resource Binding";
            default:
                return null;
        }
    }

    public MethodBean getMethodBean() {
        return methodBean;
    }

    public void setMethodBean(MethodBean methodBean) {
        this.methodBean = methodBean;
    }

    public int getSmellType() {
        return smellType;
    }

    public void setSmellType(int smellType) {
        this.smellType = smellType;
    }

    public boolean isResolved() {
        return resolved;
    }

    public void setResolved(boolean resolved) {
        this.resolved = resolved;
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public void setSourceFile(File sourceFile) {
        this.sourceFile = sourceFile;
    }
}