package adoctorr.presentation.dialog;

import com.intellij.openapi.project.Project;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class StartDialog extends JDialog {
    private JPanel contentPane;

    private JButton buttonStart;
    private JButton buttonQuit;
    private JButton buttonAbout;

    private Project project;

    /**
     * Default constructor and initializator of the dialog
     *
     * @param project
     */
    private StartDialog(Project project) {
        // Leave them as they are
        setContentPane(contentPane);
        setModal(true);
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Dimension screenSize = toolkit.getScreenSize();
        int x = (screenSize.width - getWidth()) / 3;
        int y = (screenSize.height - getHeight()) / 5;
        setLocation(x, y);

        //Pressing Enter means clicking buttonStart
        getRootPane().setDefaultButton(buttonStart);

        setTitle("aDoctor");

        this.project = project;

        // Assign all various listeners
        buttonStart.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onStartAnalysis();
            }
        });

        buttonQuit.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        });

        buttonAbout.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onAbout();
            }
        });

        // call onQuit() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onQuit();
            }
        });

        /*
        // call onQuit() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onQuit();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        */
    }

    /**
     * First and only method from the outside to be called in order to show this dialog
     *
     * @param project
     */
    public static void show(Project project) {
        StartDialog startDialog = new StartDialog(project);

        // Leave them as they are
        startDialog.pack();
        startDialog.setVisible(true);
    }

    /**
     * Start the analysis, after saving all project files
     * Called when Start Analysis button is clicked
     */
    private void onStartAnalysis() {
        dispose();

        // Starts the analysis by showing the AnalysisDialog
        AnalysisDialog.show(project);
    }

    /**
     * Exit from the plugin
     * Called when Exit button is clicked
     */
    private void onQuit() {
        dispose();
    }


    private void onAbout() {
        dispose();
        AboutDialog.show(project);
    }
}
