package io.github.pomian99.medical_clinic.dto;

// Validation rules shared by more than one command, so that a field is validated
// the same way in every request that carries it. A pattern message lives next to
// its pattern because changing one means changing the other.
final class ValidationRules {

    static final int EMAIL_MAX_LENGTH = 254;

    static final int PASSWORD_MIN_LENGTH = 8;
    static final int PASSWORD_MAX_LENGTH = 64;
    static final String PASSWORD_REGEX =
            "(?=.*\\p{Ll})(?=.*\\p{Lu})(?=.*\\d)(?=.*[^\\p{L}\\p{N}\\s])\\S+";
    static final String PASSWORD_PATTERN_MESSAGE =
            "Password must contain a lowercase letter, an uppercase letter, "
                    + "a digit and a special character, and no whitespace";

    static final int NAME_MAX_LENGTH = 50;
    static final String NAME_REGEX = "\\p{L}+([ '-]\\p{L}+)*";
    static final String FIRST_NAME_PATTERN_MESSAGE =
            "First name may contain only letters, spaces, hyphens and apostrophes";
    static final String LAST_NAME_PATTERN_MESSAGE =
            "Last name may contain only letters, spaces, hyphens and apostrophes";

    static final String PHONE_NUMBER_REGEX = "\\d{9}";
    static final String PHONE_NUMBER_PATTERN_MESSAGE = "Phone number must contain exactly 9 digits";

    private ValidationRules() {
    }
}
