package adoctor.presentation.dialog;

import adoctor.application.proposal.undo.Undo;
import adoctor.application.smell.ClassSmell;
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
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Stack;

public class CoreDriver implements StartDialog.StartCallback,
        AboutDialog.AboutCallback,
        AnalysisDialog.AnalysisCallback,
        AbortedDialog.AbortedCallback,
        NoSmellDialog.NoSmellCallback,
        SmellDialog.SmellCallback,
        RefactoringDialog.RefactoringCallback,
        SuccessDialog.SuccessCallback,
        FailureDialog.FailureCallback {

    private Project project;
    private List<File> projectFiles;
    private String[] pathEntries;
    private Stack<Undo> undoStack;
    private List<Boolean> selections;
    private String targetPackage;
    private List<ClassSmell> classSmells;

    public CoreDriver(Project project) {
        this.project = project;
        this.undoStack = new Stack<>();
    }

    public void start() {
        StartDialog.show(this, project, selections);
    }

    private void launchAnalysis() {
        // Save all files in the current project before starting the analysis
        project.save();
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandler.getInstance().refreshOpenFiles();
        //ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();

        // Get all path entries
        ProjectRootManager projectRootManager = ProjectRootManager.getInstance(project);
        VirtualFile[] paths = projectRootManager.getContentSourceRoots();
        pathEntries = new String[paths.length];
        for (int i = 0; i < pathEntries.length; i++) {
            pathEntries[i] = paths[i].getPath();
        }

        // Fetch all project files
        Collection<VirtualFile> virtualFiles = FileTypeIndex.getFiles(JavaFileType.INSTANCE, GlobalSearchScope.projectScope(project));
        projectFiles = new ArrayList<>();
        for (VirtualFile virtualFile : virtualFiles) {
            projectFiles.add(new File(virtualFile.getPath()));
        }

        AnalysisDialog.show(this, projectFiles, pathEntries, selections, targetPackage);
    }

    ////////////////StartDialog///////////////
    @Override
    public void startAnalysis(StartDialog startDialog, List<Boolean> selections, String targetPackage) {
        startDialog.dispose();
        this.selections = selections;
        this.targetPackage = targetPackage;
        launchAnalysis();
    }

    @Override
    public void startAbout(StartDialog startDialog) {
        startDialog.dispose();
        AboutDialog.show(this);
    }

    @Override
    public void startQuit(StartDialog startDialog) {
        startDialog.dispose();
    }

    /////////////////AboutDialog///////////////
    @Override
    public void aboutBack(AboutDialog aboutDialog) {
        aboutDialog.dispose();
        start();
    }

    ////////////AnalysisDialog/////////////////
    @Override
    public void analysisAbort(AnalysisDialog analysisDialog) {
        analysisDialog.dispose();
        AbortedDialog.show(this);
    }

    @Override
    public void analysisDone(AnalysisDialog analysisDialog, List<ClassSmell> classSmells) {
        this.classSmells = classSmells;
        analysisDialog.dispose();
        if (classSmells == null || classSmells.size() == 0) {
            NoSmellDialog.show(this);
        } else {
            SmellDialog.show(this, project, classSmells, selections, !undoStack.isEmpty());
        }
    }

    ////////////AbortedDialog/////////////////
    @Override
    public void abortedRestart(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
        AnalysisDialog.show(this, projectFiles, pathEntries, selections, targetPackage);
    }

    @Override
    public void abortedBack(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
        StartDialog.show(this, project, selections);
    }

    @Override
    public void abortedQuit(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
    }


    //////////////NoSmellDialog/////////////
    @Override
    public void noSmellQuit(NoSmellDialog noSmellDialog) {
        noSmellDialog.dispose();
    }

    /////////////SmellDialog//////////////
    @Override
    public void smellApply(SmellDialog smellDialog, ClassSmell targetSmell, Undo undo) {
        smellDialog.dispose();
        undoStack.push(undo);
        Document proposedDocument = undo.getDocument();
        RefactoringDialog.show(this, targetSmell, proposedDocument);
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
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                SaveAndSyncHandler.getInstance().refreshOpenFiles();
            }
        }, ModalityState.NON_MODAL);

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

    /////////////SuccessDialog//////////////
    @Override
    public void successAnalyze(SuccessDialog successDialog) {
        successDialog.dispose();
        launchAnalysis();
    }

    @Override
    public void successQuit(SuccessDialog successDialog) {
        successDialog.dispose();
    }

    @Override
    public void failureBack(FailureDialog failureDialog) {
        failureDialog.dispose();
        SmellDialog.show(this, project, classSmells, selections, !undoStack.isEmpty());
    }

    @Override
    public void failureQuit(FailureDialog failureDialog) {
        failureDialog.dispose();
    }
}
