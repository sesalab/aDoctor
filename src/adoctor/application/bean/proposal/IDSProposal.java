package adoctor.application.bean.proposal;

import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

import java.util.AbstractMap;
import java.util.List;

public class IDSProposal extends MethodProposal {
    public static final String REMOVE = "remove";
    public static final String CONTAINS_KEY = "containsKey";
    public static final String CONTAINS_VALUE = "containsValue";
    public static final String IS_EMPTY = "isEmpty";
    public static final String INDEX_OF_VALUE = "indexOfValue";
    public static final String INDEX_OF_KEY = "indexOfKey";
    public static final String SIZE = "size";
    public static final String ENTRY_SET = "entrySet";
    public static final String KEY_SET = "keySet";
    public static final String VALUES = "values";
    public static final String PUT_ALL = "putAll";
    
    private VariableDeclarationStatement proposedVarDecl;
    private List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> invocationReplacements;

    public VariableDeclarationStatement getProposedVarDecl() {
        return proposedVarDecl;
    }

    public void setProposedVarDecl(VariableDeclarationStatement proposedVarDecl) {
        this.proposedVarDecl = proposedVarDecl;
    }

    public List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> getInvocationReplacements() {
        return invocationReplacements;
    }

    public void setInvocationReplacements(List<AbstractMap.SimpleEntry<MethodInvocation, Expression>> invocationReplacements) {
        this.invocationReplacements = invocationReplacements;
    }
}
