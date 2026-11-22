package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.dto.UserUpdateCommand;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.UserInUseException;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
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
    void findAll_twoAccounts_returnsBoth() {
        // given
        List<User> accounts = List.of(account("adam.lis@example.com"), account("ewa.lis@example.com"));
        when(userRepository.findAll()).thenReturn(accounts);

        // when
        List<UserDto> result = userService.findAll();

        // then
        assertEquals(List.of("adam.lis@example.com", "ewa.lis@example.com"), result.stream().map(UserDto::email).toList());
    }

    @Test
    void findById_accountExists_returnsAccount() {
        // given
        User user = account("adam.lis@example.com");
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // when
        UserDto result = userService.findById(1L);

        // then
        assertEquals("adam.lis@example.com", result.email());
        assertEquals(Role.DOCTOR, result.role());
    }

    @Test
    void findById_accountNotFound_throwsUserNotFound() {
        // given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.findById(1L)
        );

        // then
        assertEquals("User with id 1 not found", exception.getMessage());
    }

    @Test
    void findByEmail_emailInOtherCase_looksUpLowercaseEmail() {
        // given
        User user = account("adam.lis@example.com");
        when(userRepository.findByEmail("adam.lis@example.com")).thenReturn(Optional.of(user));

        // when
        UserDto result = userService.findByEmail("Adam.Lis@Example.com");

        // then
        assertEquals("adam.lis@example.com", result.email());
    }

    @Test
    void findByEmail_accountNotFound_throwsUserNotFound() {
        // given
        when(userRepository.findByEmail("adam.lis@example.com")).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.findByEmail("adam.lis@example.com")
        );

        // then
        assertEquals("User with this email not found", exception.getMessage());
    }

    @Test
    void create_newEmail_savesAccountWithEncodedPasswordAndLowercaseEmail() {
        // given
        UserCreateCommand command = new UserCreateCommand("Adam.Lis@Example.com", "Secret#123", Role.DOCTOR);
        when(userRepository.existsByEmail("adam.lis@example.com")).thenReturn(false);
        when(passwordEncoder.encode("Secret#123")).thenReturn("hash-Secret#123");
        when(userRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        // when
        UserDto result = userService.create(command);

        // then
        verify(userRepository).save(captor.capture());
        User saved = captor.getValue();
        assertEquals("adam.lis@example.com", saved.getEmail());
        assertEquals("hash-Secret#123", saved.getPassword());
        assertEquals(Role.DOCTOR, saved.getRole());
        assertEquals("adam.lis@example.com", result.email());
    }

    @Test
    void create_emailTaken_throwsUserAlreadyExists() {
        // given
        UserCreateCommand command = new UserCreateCommand("adam.lis@example.com", "Secret#123", Role.DOCTOR);
        when(userRepository.existsByEmail("adam.lis@example.com")).thenReturn(true);

        // when
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> userService.create(command)
        );

        // then
        assertEquals("User with this email already exists", exception.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
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

    @Test
    void deleteById_accountNotUsed_deletesIt() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.existsByUserId(1L)).thenReturn(false);
        when(doctorRepository.existsByUserId(1L)).thenReturn(false);

        // when
        userService.deleteById(1L);

        // then
        verify(userRepository).deleteById(1L);
    }

    @Test
    void deleteById_accountNotFound_throwsUserNotFound() {
        // given
        when(userRepository.existsById(1L)).thenReturn(false);

        // when
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> userService.deleteById(1L)
        );

        // then
        assertEquals("User with id 1 not found", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_accountOfPatient_throwsUserInUse() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.existsByUserId(1L)).thenReturn(true);

        // when
        UserInUseException exception = assertThrows(
            UserInUseException.class,
            () -> userService.deleteById(1L)
        );

        // then
        assertEquals("User with id 1 is in use", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
    }

    @Test
    void deleteById_accountOfDoctor_throwsUserInUse() {
        // given
        when(userRepository.existsById(1L)).thenReturn(true);
        when(patientRepository.existsByUserId(1L)).thenReturn(false);
        when(doctorRepository.existsByUserId(1L)).thenReturn(true);

        // when
        UserInUseException exception = assertThrows(
            UserInUseException.class,
            () -> userService.deleteById(1L)
        );

        // then
        assertEquals("User with id 1 is in use", exception.getMessage());
        verify(userRepository, never()).deleteById(anyLong());
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
