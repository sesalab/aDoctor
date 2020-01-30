package adoctor.application.smell;

import adoctor.application.bean.ClassBean;

public abstract class ClassSmell extends Smell {
    private ClassBean classBean;

    public ClassSmell() {
        super();
    }

    public ClassSmell(String name, String shortName, String description) {
        super(name, shortName, description);
    }

    public ClassBean getClassBean() {
        return classBean;
    }

    public void setClassBean(ClassBean classBean) {
        this.classBean = classBean;
    }


}