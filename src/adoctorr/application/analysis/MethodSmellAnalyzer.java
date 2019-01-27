package adoctorr.application.analysis;

import adoctorr.application.bean.smell.MethodSmell;
import beans.MethodBean;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.MethodDeclaration;

import java.io.File;

public abstract class MethodSmellAnalyzer {

    public abstract MethodSmell analyzeMethod(MethodBean methodBean, MethodDeclaration methodDeclaration, CompilationUnit compilationUnit, File sourceFile);

}
