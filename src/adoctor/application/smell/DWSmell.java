package adoctor.application.smell;

import org.eclipse.jdt.core.dom.Expression;

public class DWSmell extends ClassSmell {
    private static final String NAME = "Durable Wakelock";
    private static final String SHORT_NAME = "DW";
    private static final String DESCRIPTION = "Durable Wakelock is present when there is a " +
            "PowerManager.WakeLock instance that calls an acquire() without setting a timeout or without calling the " +
            "corresponding release()";

    private Expression acquireExpression;

    public DWSmell() {
        super(NAME, SHORT_NAME, DESCRIPTION);
    }

    public Expression getAcquireExpression() {
        return acquireExpression;
    }

    public void setAcquireExpression(Expression acquireExpression) {
        this.acquireExpression = acquireExpression;
    }
}
