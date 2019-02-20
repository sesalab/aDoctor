package adoctor.application.analysis;

import adoctor.application.bean.Method;
import adoctor.application.bean.smell.MethodSmell;

import java.io.IOException;

public abstract class MethodSmellAnalyzer {

    public abstract MethodSmell analyzeMethod(Method method) throws IOException;

}
