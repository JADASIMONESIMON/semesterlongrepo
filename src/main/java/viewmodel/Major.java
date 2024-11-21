package viewmodel;

// Enum for Majors
public enum Major {
    CS("Computer Science"),
    CPIS("Computer and Information Systems"),
    ENGLISH("English");

    private final String displayName;

    Major(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public static Major fromString(String text) {
        for (Major major : Major.values()) {
            if (major.displayName.equalsIgnoreCase(text) || major.name().equalsIgnoreCase(text)) {
                return major;
            }
        }
        throw new IllegalArgumentException("No enum constant for text: " + text);
    }
}
