package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.dto.UserUpdateCommand;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.UserNotFoundException;
import io.github.pomian99.medical_clinic.mapper.UserMapper;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    PatientRepository patientRepository;

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    UserService userService;

    @BeforeEach
    void setUp() {
        UserMapper mapper = Mappers.getMapper(UserMapper.class);
        userService = new UserService(userRepository, patientRepository, doctorRepository, mapper, passwordEncoder);
    }

    @Test
    void update_freeEmail_changesEmail() {
        // given
        User user = account("adam.lis@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("adam.nowy@example.com")).thenReturn(false);

        // when
        UserDto result = userService.update(1L, new UserUpdateCommand("Adam.Nowy@Example.com"));

        // then
        assertEquals("adam.nowy@example.com", result.email());
        assertEquals("adam.nowy@example.com", user.getEmail());
        assertEquals(Role.DOCTOR, result.role());
    }

    @Test
    void update_sameEmail_keepsEmailWithoutCheckingOthers() {
        // given
        User user = account("adam.lis@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserDto result = userService.update(1L, new UserUpdateCommand("ADAM.LIS@example.com"));

        // then
        assertEquals("adam.lis@example.com", result.email());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void update_emailOfOtherAccount_throwsUserAlreadyExistsAndKeepsEmail() {
        // given
        User user = account("adam.lis@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsByEmail("anna.nowak@example.com")).thenReturn(true);

        // when
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> userService.update(1L, new UserUpdateCommand("anna.nowak@example.com"))
        );

        // then
        assertEquals("User with this email already exists", exception.getMessage());
        assertEquals("adam.lis@example.com", user.getEmail());
    }

    @Test
    void update_userNotFound_throwsUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.update(1L, new UserUpdateCommand("adam.nowy@example.com"))
        );

        // then
        assertEquals("User with id 1 not found", exception.getMessage());
        verify(userRepository, never()).existsByEmail(any());
    }

    private User account(String email) {
        return User.builder()
            .email(email)
            .rawPassword("Secret#123")
            .passwordEncoder(passwordEncoder)
            .role(Role.DOCTOR)
            .build();
    }
}
