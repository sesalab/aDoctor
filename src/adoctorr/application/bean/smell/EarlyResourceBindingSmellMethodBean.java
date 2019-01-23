package adoctorr.application.bean.smell;

import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.Statement;

public class EarlyResourceBindingSmellMethodBean extends SmellMethodBean {

    private Block requestBlock;
    private Statement requestStatement;

    public EarlyResourceBindingSmellMethodBean() {
        super();
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
}
