package adoctorr.presentation.dialog;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class AboutDialog extends JDialog {

    private static final String ABOUT = "" +
            "<html> " +
                "<div style=\"text-align:center\">" +
                    "This version of aDoctor is a code smell identification and refactoring plugin developed<br>" +
                    "by Emanuele Iannone at Università degli Studi di Salerno.<br>" +
                    "Please, feel free to report any bugs or suggestions at emaiannone@hotmail.it<br>" +
                    "All icons used in this plugin are made by " +
                    "<a href=\"https://www.flaticon.com/authors/good-ware\" title=\"Good Ware\">Good Ware</a> " +
                    "from <a href=\"https://www.flaticon.com/\" title=\"Flaticon\">www.flaticon.com</a> is " +
                    "licensed by <a href=\"http://creativecommons.org/licenses/by/3.0/\" " +
                   "title=\"Creative Commons BY 3.0\" target=\"_blank\">CC 3.0 BY</a>" +
                "</div>" +
            "</html>";

    private JPanel contentPane;
    private JButton buttonBack;
    private JLabel labelAbout;

    private Project project;

    private AboutDialog(Project project) {
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 4;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);
        getRootPane().setDefaultButton(buttonBack);
        setTitle("aDoctor - About");

        this.project = project;

        labelAbout.setText(ABOUT);

        buttonBack.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onBack();
            }
        });

        // call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });
    }

    public static void show(Project project) {
        AboutDialog aboutDialog = new AboutDialog(project);

        aboutDialog.pack();
        aboutDialog.setVisible(true);
    }

    private void onBack() {
        // add your code here if necessary
        dispose();
        StartDialog.show(project);
    }

    private void onQuit() {
        dispose();
    }
}
