package adoctor.application.analysis;

import adoctor.application.bean.Method;
import adoctor.application.bean.smell.MethodSmell;

public abstract class MethodSmellAnalyzer {

    public abstract MethodSmell analyzeMethod(Method method);

}
