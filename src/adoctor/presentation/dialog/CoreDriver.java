package adoctor.presentation.dialog;

import adoctor.application.bean.proposal.MethodProposal;
import adoctor.application.bean.smell.MethodSmell;
import com.intellij.diff.DiffManager;
import com.intellij.diff.DiffRequestPanel;
import com.intellij.diff.requests.SimpleDiffRequest;
import com.intellij.ide.SaveAndSyncHandlerImpl;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.eclipse.text.edits.UndoEdit;

import javax.swing.*;
import java.util.ArrayList;
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
    private Stack<UndoEdit> undoStack;
    private boolean[] selections;
    private String targetPackage;
    private ArrayList<MethodSmell> methodSmells;

    public CoreDriver(Project project) {
        this.project = project;
        this.undoStack = new Stack<>();
    }

    public void start() {
        StartDialog.show(this, project);
    }

    private void launchAnalysis() {
        // Save all files in the current project before starting the analysis
        project.save();
        FileDocumentManager.getInstance().saveAllDocuments();
        SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
        //ProjectManagerEx.getInstanceEx().blockReloadingProjectOnExternalChanges();

        AnalysisDialog.show(this, project, selections, targetPackage);
    }

    ////////////////StartDialog///////////////
    @Override
    public void startAnalysis(StartDialog startDialog, boolean[] selections, String targetPackage) {
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
    public void analysisDone(AnalysisDialog analysisDialog, ArrayList<MethodSmell> methodSmells) {
        this.methodSmells = methodSmells;
        analysisDialog.dispose();
        if (methodSmells == null || methodSmells.size() == 0) {
            NoSmellDialog.show(this);
        } else {
            SmellDialog.show(this, project, methodSmells, selections, !undoStack.isEmpty());
        }
    }

    ////////////AbortedDialog/////////////////
    @Override
    public void abortedQuit(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
    }

    @Override
    public void abortedRestart(AbortedDialog abortedDialog) {
        abortedDialog.dispose();
        AnalysisDialog.show(this, project, selections, targetPackage);
    }


    //////////////NoSmellDialog/////////////
    @Override
    public void noSmellQuit(NoSmellDialog noSmellDialog) {
        noSmellDialog.dispose();
    }

    /////////////SmellDialog//////////////
    @Override
    public void smellApply(SmellDialog smellDialog, MethodProposal methodProposal) {
        smellDialog.dispose();
        RefactoringDialog.show(this, methodProposal, selections);
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
        //TODO Fai robe con l'undo
        // 1.Segui lo stesso pattern di quando chiami RefactoringDialog, solo che chiami UndoDialog
        // 2.Essa deve fare cose analoghe a RefactoringDialog
        // 3.Al termine, rilancia di nuovo l'analisi con launchAnalysis()
        System.out.println("E qui faccio l'undo");
    }

    /////////////RefactoringDialog//////////////
    @Override
    public void refactoringDone(RefactoringDialog refactoringDialog, UndoEdit undoEdit) {
        refactoringDialog.dispose();

        // Refreshes the Editor in order to reflect the changes to the files
        ApplicationManager.getApplication().invokeLater(new Runnable() {
            @Override
            public void run() {
                SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
            }
        }, ModalityState.NON_MODAL);

        if (undoEdit != null) {
            undoStack.push(undoEdit);
            SuccessDialog.show(this);
        } else {
            FailureDialog.show(this);
        }

        /* Fired when all modal dialogs are closed
        TransactionGuardImpl.submitTransaction(Disposer.newDisposable(), new Runnable() {
            @Override
            public void run() {
                SaveAndSyncHandlerImpl.getInstance().refreshOpenFiles();
            }
        });
        */
        // VirtualFileManager.getInstance().refreshWithoutFileWatcher(false);
        // ProjectManagerEx.getInstanceEx().unblockReloadingProjectOnExternalChanges();
        /* Useless...
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
        SmellDialog.show(this, project, methodSmells, selections, !undoStack.isEmpty());
    }

    @Override
    public void failureQuit(FailureDialog failureDialog) {
        failureDialog.dispose();
    }
}
