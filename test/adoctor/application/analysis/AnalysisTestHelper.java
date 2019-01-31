package adoctor.application.analysis;

import beans.ClassBean;
import beans.MethodBean;
import beans.PackageBean;
import process.FolderToJavaProjectConverter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class AnalysisTestHelper {

    public static ArrayList<PackageBean> getPackageBeans(String testDirectory, String testPackage, String testClass) throws IOException {
        // Phase 1: ArrayList<PackageBean>
        File testDirectoryFile = new File(testDirectory);
        ArrayList<PackageBean> totalPackages = FolderToJavaProjectConverter.convert(testDirectoryFile.getAbsolutePath());
        // belongingClass was not set in aDoctor API: this is just a fix
        for (PackageBean packageBean : totalPackages) {
            for (ClassBean classBean : packageBean.getClasses()) {
                for (MethodBean methodBean : classBean.getMethods()) {
                    methodBean.setBelongingClass(classBean);
                }
            }
        }
        // Phase 2: Removal of useless packages and classes
        ArrayList<PackageBean> testPackages = new ArrayList<>();
        for (PackageBean packageBean : totalPackages) {
            if (packageBean.getName().equals(testPackage)) {
                testPackages.add(packageBean);
            }
        }
        testPackages.get(0).getClasses().removeIf(classBean -> !classBean.getName().equals(testClass));
        return testPackages;
    }

    public static ArrayList<MethodBean> getMethodBeans(ArrayList<PackageBean> packages) {
        // Phase 3: Building of a single list of MethodBeans
        ArrayList<MethodBean> methodBeans = new ArrayList<>();
        for (PackageBean packageBean : packages) {
            for (ClassBean classBean : packageBean.getClasses()) {
                methodBeans.addAll(classBean.getMethods());
            }
        }
        return methodBeans;
    }
}
