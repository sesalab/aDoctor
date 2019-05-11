package adoctor.application.smell;

abstract class Smell {
    private String name;
    private String description;

    Smell() {
        name = "";
        description = "";
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
}
