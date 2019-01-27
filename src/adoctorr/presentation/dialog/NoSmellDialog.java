package adoctorr.presentation.dialog;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class NoSmellDialog extends JDialog {
    public static final String TITLE = "aDoctor - No Smell";

    private NoSmellCallback noSmellCallback;

    private JPanel contentPane;
    private JButton buttonQuit;

    public static void show(NoSmellCallback noSmellCallback) {
        NoSmellDialog noSmellDialog = new NoSmellDialog(noSmellCallback);

        noSmellDialog.pack();
        noSmellDialog.setVisible(true);
    }

    private NoSmellDialog(NoSmellCallback noSmellCallback) {
        this.noSmellCallback = noSmellCallback;

        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) * 2 / 5;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonQuit);
        setTitle(TITLE);

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });

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
