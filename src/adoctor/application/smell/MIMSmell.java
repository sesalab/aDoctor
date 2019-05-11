package adoctor.application.smell;

import org.eclipse.jdt.core.dom.MethodDeclaration;

public class MIMSmell extends ClassSmell {
    private static final String NAME = "Member Ignoring Method";
    private static final String DESCRIPTION = "Member Ignoring Method is present when a non static method does not " +
            "use at all instance variables and other non static methods.";

    private MethodDeclaration smellyMethod;

    public MIMSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public MethodDeclaration getSmellyMethod() {
        return smellyMethod;
    }

    public void setSmellyMethod(MethodDeclaration smellyMethod) {
        this.smellyMethod = smellyMethod;
    }
}
