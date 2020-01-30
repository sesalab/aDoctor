package adoctor.application.smell;

abstract class Smell {
    private String name;
    private String shortName;
    private String description;

    public Smell() {
        this.name = "";
        this.shortName = "";
        this.description = "";
    }

    public Smell(String name, String shortName, String description) {
        this.name = name;
        this.shortName = shortName;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    void setDescription(String description) {
        this.description = description;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }
}
