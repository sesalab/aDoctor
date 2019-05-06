package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class ERBSmell extends ClassSmell {
    public static final String NAME = "Early Resource Binding";
    public static final String DESCRIPTION = "Early Resource Binding is present when an " +
            "Android system service is used in the onCreate(Bundle) method of an Activity subclass.";
    public static final String ONCREATE_NAME = "onCreate";
    public static final String ONCREATE_TYPE = "void";
    public static final String ONCREATE_SCOPE1 = "public";
    public static final String ONCREATE_SCOPE2 = "protected";
    public static final String ONCREATE_ARGUMENT_TYPE = "Bundle";
    public static final String ONRESUME_NAME = "onResume";
    public static final String GPS_REQUEST_METHOD_NAME = "requestLocationUpdates";

    private Block requestBlock;
    private Statement requestStatement;
    private MethodDeclaration onCreate;

    public ERBSmell() {
        super();
        setName(NAME);
        setDescription(DESCRIPTION);
    }

    public Block getRequestBlock() {
        return requestBlock;
    }

    public void setRequestBlock(Block requestBlock) {
        this.requestBlock = requestBlock;
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
