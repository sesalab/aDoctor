package adoctor.application.smell;

import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

public class LTSmell extends ClassSmell {
    private static final String NAME = "Leaking Thread";
    private static final String DESCRIPTION = "Leaking Thread is present when in an Activity there is an instance level " +
            "thread that is launched and never stopped, so not allowing the GC to remove it even when that activity instance " +
            "has been removed.";

    private VariableDeclarationFragment smellyVariableDeclarationFragment;

    public LTSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public VariableDeclarationFragment getSmellyVariableDeclarationFragment() {
        return smellyVariableDeclarationFragment;
    }

    public void setSmellyVariableDeclarationFragment(VariableDeclarationFragment smellyVariableDeclarationFragment) {
        this.smellyVariableDeclarationFragment = smellyVariableDeclarationFragment;
    }
}
