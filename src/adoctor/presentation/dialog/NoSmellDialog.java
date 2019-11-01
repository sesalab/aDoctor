package adoctor.presentation.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NoSmellDialog extends AbstractDialog {
    private static final String TITLE = "aDoctor - No Smell";

    private NoSmellCallback noSmellCallback;

    private JPanel contentPane;
    private JButton buttonQuit;

    public static void show(NoSmellCallback noSmellCallback) {
        NoSmellDialog noSmellDialog = new NoSmellDialog(noSmellCallback);
        noSmellDialog.showInCenter();
    }

    private NoSmellDialog(NoSmellCallback noSmellCallback) {
        this.noSmellCallback = noSmellCallback;
        init();
    }

    private void init() {
        super.init(contentPane, TITLE, buttonQuit);

        buttonQuit.addActionListener(e -> onQuit());
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void onQuit() {
        noSmellCallback.noSmellQuit(this);
    }

    interface NoSmellCallback {
        void noSmellQuit(NoSmellDialog noSmellDialog);
    }
}
