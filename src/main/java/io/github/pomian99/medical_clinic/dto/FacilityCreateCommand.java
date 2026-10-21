package io.github.pomian99.medical_clinic.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FacilityCreateCommand(
        @NotBlank(message = "Name cannot be blank")
        @Size(max = 100, message = "Name cannot be longer than {max} characters")
        String name,

        @NotBlank(message = "City cannot be blank")
        @Size(max = 100, message = "City cannot be longer than {max} characters")
        String city,

        // The address below is optional; a missing field is simply left empty.
        @Pattern(regexp = "\\d{2}-\\d{3}", message = "Zip code must have the format 00-000")
        String zipCode,

        @Size(max = 100, message = "Street cannot be longer than {max} characters")
        String street,

        @Size(max = 10, message = "Number cannot be longer than {max} characters")
        String number
) {
}
