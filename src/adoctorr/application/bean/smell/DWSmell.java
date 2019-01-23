package adoctorr.application.bean.smell;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class DWSmell extends MethodSmell {

    private Block acquireBlock;
    private Statement acquireStatement;

    public DWSmell() {
        super();
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
