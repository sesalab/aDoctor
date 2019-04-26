package adoctor.application.proposal.undo;

import org.eclipse.jface.text.Document;
import org.eclipse.text.edits.UndoEdit;

public class Undo {
    private UndoEdit undoEdit;
    private Document document;

    public Undo(UndoEdit undoEdit, Document document) {
        this.undoEdit = undoEdit;
        this.document = document;
    }

    public UndoEdit getUndoEdit() {
        return undoEdit;
    }

    public void setUndoEdit(UndoEdit undoEdit) {
        this.undoEdit = undoEdit;
    }

    public Document getDocument() {
        return document;
    }

    public void setDocument(Document document) {
        this.document = document;
    }
}
