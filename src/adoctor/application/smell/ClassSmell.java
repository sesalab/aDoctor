package adoctor.application.smell;

import adoctor.application.bean.ClassBean;

public abstract class ClassSmell extends Smell {
    private ClassBean classBean;

    public ClassBean getClassBean() {
        return classBean;
    }

    public void setClassBean(ClassBean classBean) {
        this.classBean = classBean;
    }


}