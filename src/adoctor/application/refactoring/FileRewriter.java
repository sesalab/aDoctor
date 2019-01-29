package adoctor.application.refactoring;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class FileRewriter {

    public FileRewriter() {

    }

    public boolean writeText(File sourceFile, String text) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(sourceFile, false));
        pw.print(text);
        pw.flush(); // Important
        return true;
    }

}
