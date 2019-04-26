package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.VariableDeclarationStatement;

public class IDSSmell extends MethodSmell {
    public static final String NAME = "Inefficient Data Structure";
    public static final String DESCRIPTION = "Inefficient Data Structure is present when a HashMap<Integer, Object> " +
            "is used. This affects performances with the autoboxing problem.";
    public static final String HASHMAP = "HashMap";
    public static final String INTEGER = "Integer";
    public static final String SPARSE_ARRAY = "SparseArray";
    public static final String IMPORT = "android.util.SparseArray";
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

    private VariableDeclarationStatement smellyVarDecl;

    public IDSSmell() {
        super();
        setSmellName(NAME);
        setSmellDescription(DESCRIPTION);
    }

    public VariableDeclarationStatement getSmellyVarDecl() {
        return smellyVarDecl;
    }

    public void setSmellyVarDecl(VariableDeclarationStatement smellyVarDecl) {
        this.smellyVarDecl = smellyVarDecl;
    }
}
