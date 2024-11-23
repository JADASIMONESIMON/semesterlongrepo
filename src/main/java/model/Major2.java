package model;

public enum Major2 {
    CS("Computer Science"),
    CPIS("Computer and Information Systems"),
    ENGLISH("English");

    private final String displayName;

    Major2(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
