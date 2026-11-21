package io.github.pomian99.medical_clinic.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Setter(AccessLevel.NONE)
    @Column(nullable = false)
    private String password;

    // Empty for an account created on its own: it gets a role together with
    // the doctor (or patient) that is attached to it.
    @Enumerated(EnumType.STRING)
    private Role role;

    @Builder
    private User(String email, String rawPassword, PasswordEncoder passwordEncoder, Role role) {
        this.email = normalizeEmail(email);
        this.password = encodePassword(rawPassword, passwordEncoder);
        this.role = role;
    }

    public static String normalizeEmail(String email) {
        return email == null ? null : email.toLowerCase(Locale.ROOT);
    }

    public void setEmail(String email) {
        this.email = normalizeEmail(email);
    }

    public void changePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        this.password = encodePassword(rawPassword, passwordEncoder);
    }

    private static String encodePassword(String rawPassword, PasswordEncoder passwordEncoder) {
        if (rawPassword == null || rawPassword.isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        return passwordEncoder.encode(rawPassword);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof User other)) {
            return false;
        }
        return id != null && id.equals(other.getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
