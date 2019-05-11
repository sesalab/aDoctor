package adoctor.application.analysis.analyzers;

import adoctor.application.bean.ClassBean;
import adoctor.application.smell.ClassSmell;

public abstract class ClassSmellAnalyzer {

    //TODO Medium Return more than one smell per kind?
    public abstract ClassSmell analyze(ClassBean classBean);

}
