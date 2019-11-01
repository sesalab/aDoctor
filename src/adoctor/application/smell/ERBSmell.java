package adoctor.application.smell;

import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class ERBSmell extends ClassSmell {
    private static final String NAME = "Early Resource Binding";
    private  static final String DESCRIPTION = "Early Resource Binding is present when an " +
            "Android system service is used in the onCreate(Bundle) method of an Activity subclass.";

    private Statement requestStatement;
    private MethodDeclaration onCreate;

    public ERBSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public Statement getRequestStatement() {
        return requestStatement;
    }

    public void setRequestStatement(Statement requestStatement) {
        this.requestStatement = requestStatement;
    }

    public MethodDeclaration getOnCreate() {
        return onCreate;
    }

    public void setOnCreate(MethodDeclaration onCreate) {
        this.onCreate = onCreate;
    }
}
