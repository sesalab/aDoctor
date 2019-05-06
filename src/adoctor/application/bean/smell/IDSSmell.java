package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class IDSSmell extends ClassSmell {
    private static final String NAME = "Inefficient Data Structure";
    private static final String DESCRIPTION = "Inefficient Data Structure is present when a HashMap<Integer, Object> " +
            "is used. This affects performances due to intensive autoboxing usage.";

    private VariableDeclarationStatement smellyVarDecl;

    public IDSSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public VariableDeclarationStatement getSmellyVarDecl() {
        return smellyVarDecl;
    }

    public void setSmellyVarDecl(VariableDeclarationStatement smellyVarDecl) {
        this.smellyVarDecl = smellyVarDecl;
    }
}
