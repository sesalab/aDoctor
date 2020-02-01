package adoctor.presentation;

import adoctor.application.analytics.MeasurementManager;
import adoctor.application.proposal.undo.Undo;
import adoctor.application.smell.ClassSmell;
import adoctor.presentation.dialog.*;
import adoctor.presentation.pref.PreferenceManager;
import com.intellij.ide.SaveAndSyncHandler;
import com.intellij.ide.highlighter.JavaFileType;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.FileTypeIndex;
import com.intellij.psi.search.GlobalSearchScope;
import org.eclipse.jface.text.Document;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Stack;
import java.util.UUID;
import java.util.stream.Collectors;

public class CoreDriver implements StartDialog.StartCallback,
        SettingsDialog.SettingsCallback,
        AnalysisDialog.AnalysisCallback,
        AbortedDialog.AbortedCallback,
        NoSmellDialog.NoSmellCallback,
        SmellDialog.SmellCallback,
        RefactoringDialog.RefactoringCallback,
        SuccessDialog.SuccessCallback,
        FailureDialog.FailureCallback {

    private Project project;
    private MeasurementManager measurementManager;
    private PreferenceManager preferenceManager;
    private Stack<Undo> undoStack;
    // TODO: Convert to a Map
    private List<Boolean> selectedSmells;

    private String targetPackage;

    private List<ClassSmell> smellInstances;

    public CoreDriver(Project project, String pluginId) {
        this.project = project;
        this.measurementManager = new MeasurementManager(UUID.nameUUIDFromBytes(project.getName().getBytes()).toString());
        this.preferenceManager = new PreferenceManager(pluginId);
        this.undoStack = new Stack<>();
        this.selectedSmells = preferenceManager.getSavedSelectedSmells();
    }

    private static void save(Project project) {
        System.out.print("Saving all files...");
        project.save();
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        //ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();
        System.out.println("saved!");
    }

    ////////////////StartDialog///////////////
    @Override
    public void runAnalysis(StartDialog startDialog, List<Boolean> selectedSmells, String targetPackage) {
        startDialog.dispose();
        this.selectedSmells = selectedSmells;
        this.targetPackage = targetPackage;
        preferenceManager.setSavedSelectedSmells(selectedSmells);
        runAnalysis();
    }

    @Override
    public void startQuit(StartDialog startDialog) {
        startDialog.dispose();
    }

    @Override
    public void startSettings(StartDialog startDialog) {
        startDialog.dispose();
        SettingsDialog.show(this, preferenceManager.isSavedStats());
    }

    /////////////////SettingsDialog///////////////
    @Override
    public void settingsBack(SettingsDialog settingsDialog) {
        settingsDialog.dispose();
        start();
    }

    ////////////AnalysisDialog/////////////////
    @Override
    public void analysisAbort(AnalysisDialog analysisDialog) {
        analysisDialog.dispose();
        AbortedDialog.show(this);
    }

    @Override
    public void settingsSave(SettingsDialog settingsDialog, boolean statsChecked) {
        settingsDialog.dispose();
        preferenceManager.setSavedStats(statsChecked);
        start();
    }

    @Override
    public void analysisDone(AnalysisDialog analysisDialog, List<ClassSmell> classSmells) {
        this.smellInstances = classSmells;
        analysisDialog.dispose();
        if (classSmells == null || classSmells.size() == 0) {
            NoSmellDialog.show(this);
        } else {
            SmellDialog.show(this, project, classSmells, selectedSmells, !undoStack.isEmpty());
        }
    }

    @Override
    public void abortedBack(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
        start();
    }

    @Override
    public void abortedQuit(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
    }

    //////////////NoSmellDialog/////////////
    @Override
    public void noSmellBack(NoSmellDialog noSmellDialog) {
        noSmellDialog.dispose();
        start();
    }

    @Override
    public void noSmellQuit(NoSmellDialog noSmellDialog) {
        noSmellDialog.dispose();
    }

    ////////////AbortedDialog/////////////////
    @Override
    public void abortedRestart(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
        runAnalysis();
        //AnalysisDialog.show(this, projectFiles, pathEntries, selectedSmells, targetPackage);
    }

    @Override
    public void smellBack(SmellDialog smellDialog) {
        smellDialog.dispose();
        start();
    }

    @Override
    public void smellQuit(SmellDialog smellDialog) {
        smellDialog.dispose();
    }

    @Override
    public void smellUndo(SmellDialog smellDialog) {
        //TODO Low Implement UNDO
        System.out.println("E qui faccio l'undo");
    }

    /////////////RefactoringDialog//////////////
    @Override
    public void refactoringDone(RefactoringDialog refactoringDialog, Boolean result) {
        refactoringDialog.dispose();

        // Refreshes the Editor in order to reflect the changes to the files
        ApplicationManager.getApplication().invokeLater(() -> SaveAndSyncHandler.getInstance().refreshOpenFiles(), ModalityState.NON_MODAL);

        if (result) {
            SuccessDialog.show(this);
        } else {
            undoStack.pop();
            FailureDialog.show(this);
        }

        /* Fired when all modal dialogs are closed
        TransactionGuardImpl.submitTransaction(Disposer.newDisposable(), new Runnable() {
            @Override
            public void run() {
                SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
            }
        });
         VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
         ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();

            Document[] documents = FileDocumentManager.getInstance().getUnsavedDocuments();
            System.out.println(documents.length);
            for (Document document : documents) {
                FileDocumentManager.getInstance().saveDocument(document);
            }
        */
    }

    @Override
    public void refactoringQuit(RefactoringDialog refactoringDialog) {
        refactoringDialog.dispose();
    }

    /////////////SmellDialog//////////////
    @Override
    public void smellApply(SmellDialog smellDialog, ClassSmell targetSmell, Undo undo) {
        smellDialog.dispose();
        undoStack.push(undo);
        Document proposedDocument = undo.getDocument();

        // Send refactoring usage statistics
        if (preferenceManager.isSavedStats()) {
            try {
                this.measurementManager.sendRefactoringData(targetSmell.getShortName().toLowerCase());
            } catch (IOException e) {
                System.out.println("Refactoring data has not been sent");
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot send any data");
        }

        RefactoringDialog.show(this, targetSmell, proposedDocument);
    }

    @Override
    public void successBack(SuccessDialog successDialog) {
        successDialog.dispose();
        start();
    }

    @Override
    public void successQuit(SuccessDialog successDialog) {
        successDialog.dispose();
    }

    /////////////SuccessDialog//////////////
    @Override
    public void successAnalyze(SuccessDialog successDialog) {
        successDialog.dispose();
        runAnalysis();
    }

    @Override
    public void failureQuit(FailureDialog failureDialog) {
        failureDialog.dispose();
    }

    @Override
    public void failureBack(FailureDialog failureDialog) {
        failureDialog.dispose();
        SmellDialog.show(this, project, smellInstances, selectedSmells, !undoStack.isEmpty());
    }

    ///////////////////////Helper methods///////////////////////
    public void start() {
        StartDialog.show(this, project, selectedSmells);
    }

    private void runAnalysis() {
        // Save all files in the current project before starting the analysis
        save(this.project);

        // Fetch all project files
        List<File> projectFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project))
                .stream()
                .map(vf -> new File(vf.getPath()))
                .filter(File::isFile)
                .collect(Collectors.toList());

        // Get all path entries
        String[] pathEntries = Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots())
                .map(VirtualFile::getPath)
                .toArray(String[]::new);

        // Send analysis usage statistics
        if (preferenceManager.isSavedStats()) {
            try {
                this.measurementManager.sendAnalysisData(this.selectedSmells);
                System.out.println("Data successfully sent!");
            } catch (IOException e) {
                System.out.println("Analysis data has not been sent");
                e.printStackTrace();
            }
        } else {
            System.out.println("Cannot send any data");
        }
        AnalysisDialog.show(this, projectFiles, pathEntries, selectedSmells, targetPackage);
    }
}
