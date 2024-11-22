package viewmodel;
// Enum for Majors
public enum Major {
    CS("Computer Science"),
    CPIS("Computer and Information Systems"),
    ENGLISH("English");

    private final String displayName;

    // Constructor for assigning display names
    Major(String displayName) {
        this.displayName = displayName;
    }

    // Getter for display names
    public String getDisplayName() {
        return displayName;
    }

    // Method to retrieve enum from a string input
    public static Major fromString(String text) {
        for (Major major : Major.values()) {
            if (major.displayName.equalsIgnoreCase(text) || major.name().equalsIgnoreCase(text)) {
                return major;
            }
        }
        throw new IllegalArgumentException("No enum constant for text: " + text);
    }
}
