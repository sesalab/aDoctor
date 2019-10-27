package adoctor.application.smell;

import org.apache.commons.lang3.tuple.Pair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;

public class ISSmell extends ClassSmell {
    private static final String NAME = "Internal Setter";
    private static final String DESCRIPTION = "Internal Setter is present when non static method calls a setter " +
            "method of an instance variable instead of directly changing its value.";

    private MethodInvocation smellyCall;
    private Pair<MethodDeclaration, String> smellySetter;

    public ISSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public MethodInvocation getSmellyCall() {
        return smellyCall;
    }

    public void setSmellyCall(MethodInvocation smellyCall) {
        this.smellyCall = smellyCall;
    }

    public Pair<MethodDeclaration, String> getSmellySetter() {
        return smellySetter;
    }

    public void setSmellySetter(Pair<MethodDeclaration, String> smellySetter) {
        this.smellySetter = smellySetter;
    }
}
