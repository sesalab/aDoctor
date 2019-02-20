package adoctor.application.bean.smell;

import org.eclipse.jdt.core.dom.SimpleName;

import java.util.List;

public class IDSSmell extends MethodSmell {
    public static final String NAME = "Inefficient Data Structure";
    public static final String DESCRIPTION = "Inefficient Data Structure is present when a HashMap<Integer, Object> " +
            "is used. This affects performances with the autoboxing problem.";
    public static final String HASHMAP = "HashMap";
    public static final String INTEGER = "Integer";

    private List<SimpleName> names;

    public IDSSmell() {
        super();
        setSmellName(NAME);
        setSmellDescription(DESCRIPTION);
        names = null;
    }

    public List<SimpleName> getNames() {
        return names;
    }

    public void setNames(List<SimpleName> names) {
        this.names = names;
    }
}
