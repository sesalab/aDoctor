package adoctor.application.bean;

public class Method extends Bean {

    private beans.MethodBean legacyMethodBean;

    public Method() {
    }

    public beans.MethodBean getLegacyMethodBean() {
        return legacyMethodBean;
    }

    public void setLegacyMethodBean(beans.MethodBean legacyMethodBean) {
        this.legacyMethodBean = legacyMethodBean;
    }
}
