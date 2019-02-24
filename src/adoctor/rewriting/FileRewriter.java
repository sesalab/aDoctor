package adoctor.rewriting;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;

public class FileRewriter {

    public boolean writeText(File sourceFile, String text) throws FileNotFoundException {
        PrintWriter pw = new PrintWriter(new FileOutputStream(sourceFile, false));
        pw.print(text);
        pw.flush(); // Important

        /*
        PrintWriter pw = new PrintWriter(new FileOutputStream(sourceFile, false));
        Runnable thread = new Runnable() {
            @Override
            public void run() {
                System.out.println("Ciao");
                pw.print(text);
                pw.flush(); // Important
            }
        };
        //ApplicationManager.getApplication().invokeAndWait(thread);
        //TransactionGuardImpl.getInstance().submitTransactionAndWait(thread);
        */
        return true;
    }

}
