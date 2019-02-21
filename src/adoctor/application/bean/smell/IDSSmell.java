package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class IDSSmell extends MethodSmell {
    public static final String NAME = "Inefficient Data Structure";
    public static final String DESCRIPTION = "Inefficient Data Structure is present when a HashMap<Integer, Object> " +
            "is used. This affects performances with the autoboxing problem.";
    public static final String HASHMAP = "HashMap";
    public static final String INTEGER = "Integer";
    public static final String SPARSE_ARRAY = "SparseArray";

    private VariableDeclarationStatement variableDeclarationStatement;

    public IDSSmell() {
        super();
        setSmellName(NAME);
        setSmellDescription(DESCRIPTION);
        variableDeclarationStatement = null;
    }

    public VariableDeclarationStatement getVariableDeclarationStatement() {
        return variableDeclarationStatement;
    }

    public void setVariableDeclarationStatement(VariableDeclarationStatement variableDeclarationStatement) {
        this.variableDeclarationStatement = variableDeclarationStatement;
    }
}
