package adoctor.application.bean.smell;

import adoctor.application.bean.Method;

public abstract class MethodSmell extends Smell {

    private Method method;
    private String smellName;
    private String smellDescription;

    public MethodSmell() {
        method = null;
        smellName = "";
        smellDescription = "";
    }

    public Method getMethod() {
        return method;
    }

    public void setMethod(Method method) {
        this.method = method;
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
}