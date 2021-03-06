package info.doseamigos.doseevents;

/**
 * Enum that holds specific Event type.
 */
public enum EventType {

    /**
     * Represents that the user took the medication.
     */
    TAKEN,

    /**
     * Represents that the user intentionally skipped the medication.
     */
    SKIPPED,

    /**
     * Represents that the user missed their dose at the specific scheduled time.
     */
    MISSED,

    /**
     * When a user undoes a dose.
     */
    UNDONE;

    public static EventType getByString(String eventName) {
        if (eventName == null) {
            return null;
        }
        for (EventType type : values()) {
            if (type.name().equals(eventName.toUpperCase())) {
                return type;
            }
        }
        return null;
    }
}
