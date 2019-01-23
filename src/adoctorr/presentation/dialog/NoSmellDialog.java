package adoctorr.presentation.dialog;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class NoSmellDialog extends JDialog {
    private JPanel contentPane;
    private JButton buttonQuit;

    private NoSmellDialog() {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) * 2 / 5;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonQuit);
        setTitle("aDoctor - No Smell");

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });

        // call onQuit() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    private void onQuit() {
        dispose();
    }

    public static void show(Project project) {
        NoSmellDialog noSmellDialog = new NoSmellDialog();

        noSmellDialog.pack();
        noSmellDialog.setVisible(true);
    }
}
