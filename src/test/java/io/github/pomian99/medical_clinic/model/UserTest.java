package io.github.pomian99.medical_clinic.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserTest {

    @Mock
    PasswordEncoder passwordEncoder;

    @Test
    void builder_validPassword_storesEncodedPassword() {
        // given
        when(passwordEncoder.encode("Secret123!")).thenReturn("hash-Secret123!");

        // when
        User user = User.builder()
            .email("jankow@poczta.pl")
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .role(Role.PATIENT)
            .build();

        // then
        assertEquals("hash-Secret123!", user.getPassword());
    }

    @Test
    void builder_mixedCaseEmail_storesLowercaseEmail() {
        // when
        User user = User.builder()
            .email("Jan.Kowalski@Poczta.PL")
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .build();

        // then
        assertEquals("jan.kowalski@poczta.pl", user.getEmail());
    }

    @Test
    void setEmail_mixedCaseEmail_storesLowercaseEmail() {
        // given
        User user = User.builder()
            .email("jankow@poczta.pl")
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .build();

        // when
        user.setEmail("Janusz.Nowak@Poczta.PL");

        // then
        assertEquals("janusz.nowak@poczta.pl", user.getEmail());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void builder_blankPassword_throwsWithoutEncoding(String blank) {
        // when
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> User.builder()
                .email("jankow@poczta.pl")
                .rawPassword(blank)
                .passwordEncoder(passwordEncoder)
                .build()
        );

        // then
        assertEquals("Password cannot be blank", exception.getMessage());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void changePassword_validPassword_storesEncodedPassword() {
        // given
        User user = User.builder()
            .email("jankow@poczta.pl")
            .rawPassword("OldSecret123!")
            .passwordEncoder(passwordEncoder)
            .build();
        when(passwordEncoder.encode("NewSecret123!")).thenReturn("hash-NewSecret123!");

        // when
        user.changePassword("NewSecret123!", passwordEncoder);

        // then
        assertEquals("hash-NewSecret123!", user.getPassword());
    }

    @ParameterizedTest
    @NullAndEmptySource
    @ValueSource(strings = {"   "})
    void changePassword_blankPassword_throwsAndKeepsOldPassword(String blank) {
        // given
        when(passwordEncoder.encode("OldSecret123!")).thenReturn("hash-OldSecret123!");
        User user = User.builder()
            .email("jankow@poczta.pl")
            .rawPassword("OldSecret123!")
            .passwordEncoder(passwordEncoder)
            .build();

        // when
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> user.changePassword(blank, passwordEncoder)
        );

        // then
        assertEquals("Password cannot be blank", exception.getMessage());
        assertEquals("hash-OldSecret123!", user.getPassword());
    }
}
