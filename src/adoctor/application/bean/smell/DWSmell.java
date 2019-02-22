package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Statement;

public class DWSmell extends MethodSmell {
    public static final String NAME = "Durable Wakelock";
    public static final String DESCRIPTION = "Durable Wakelock is present when there is a " +
            "PowerManager.WakeLock instance that calls an acquire() without setting a timeout or without calling the " +
            "corresponding release()";
    public static final String ACQUIRE_NAME = "acquire";
    public static final String RELEASE_NAME = "release";
    public static final String WAKELOCK_CLASS = "PowerManager.WakeLock";

    private Block acquireBlock;
    private Statement acquireStatement;

    public DWSmell() {
        super();
        setSmellName(NAME);
        setSmellDescription(DESCRIPTION);
    }

    public Block getAcquireBlock() {
        return acquireBlock;
    }

    public void setAcquireBlock(Block acquireBlock) {
        this.acquireBlock = acquireBlock;
    }

    public Statement getAcquireStatement() {
        return acquireStatement;
    }

    public void setAcquireStatement(Statement acquireStatement) {
        this.acquireStatement = acquireStatement;
    }
}
