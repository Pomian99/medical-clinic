package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.exception.PatientAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.PatientHasVisitsException;
import io.github.pomian99.medical_clinic.exception.PatientNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    PatientRepository patientRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    VisitRepository visitRepository;

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    PatientService patientService;

    @BeforeEach
    void setUp() {
        PatientMapper mapper = Mappers.getMapper(PatientMapper.class);
        patientService = new PatientService(patientRepository, userRepository, visitRepository, doctorRepository,
            mapper, passwordEncoder);
    }

    @Test
    void findAll_twoPatientsExist_returnsBothPatients() {
        // given
        Patient jan = Patient.builder()
            .id(1L)
            .firstName("Jan")
            .build();
        Patient anna = Patient.builder()
            .id(2L)
            .firstName("Anna")
            .build();
        when(patientRepository.findAll()).thenReturn(List.of(jan, anna));

        // when
        List<PatientDto> result = patientService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Anna", result.get(1).firstName());
    }

    @Test
    void findById_patientExists_returnsPatient() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .firstName("Jan")
            .lastName("Kowalski")
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // when
        PatientDto result = patientService.findById(1L);

        // then
        assertEquals("Jan", result.firstName());
        assertEquals("Jan Kowalski", result.fullName());
    }

    @Test
    void findById_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientService.findById(1L)
        );

        // then
        assertEquals("Patient with id 1 not found", exception.getMessage());
    }

    @Test
    void findByEmail_patientExists_returnsPatient() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .rawPassword("Secret123!")
                    .passwordEncoder(passwordEncoder)
                    .build()
            )
            .build();
        when(patientRepository.findByUserEmail("jankow@poczta.pl")).thenReturn(Optional.of(patient));

        // when
        PatientDto result = patientService.findByEmail("jankow@poczta.pl");

        // then
        assertEquals(1L, result.id());
        assertEquals("jankow@poczta.pl", result.email());
    }

    @Test
    void findByEmail_emailInUppercase_looksUpLowercaseEmail() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .rawPassword("Secret123!")
                    .passwordEncoder(passwordEncoder)
                    .build()
            )
            .build();
        when(patientRepository.findByUserEmail("jankow@poczta.pl")).thenReturn(Optional.of(patient));

        // when
        PatientDto result = patientService.findByEmail("JANKOW@Poczta.pl");

        // then
        assertEquals(1L, result.id());
    }

    @Test
    void findByEmail_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findByUserEmail("jankow@poczta.pl")).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientService.findByEmail("jankow@poczta.pl")
        );

        // then
        assertEquals("Patient with this email not found", exception.getMessage());
    }

    @Test
    void findByFragment_lastNameFragment_returnsMatchingPatient() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .lastName("Kowalski")
            .build();
        when(patientRepository.searchByFragment("Kowal")).thenReturn(List.of(patient));

        // when
        List<PatientDto> result = patientService.findByFragment("Kowal");

        // then
        assertEquals(1, result.size());
        assertEquals("Kowalski", result.get(0).lastName());
    }

    @Test
    void findByFragment_percentAndUnderscore_searchesForThemLiterally() {
        // given
        when(patientRepository.searchByFragment("50!%!_")).thenReturn(List.of());

        // when
        List<PatientDto> result = patientService.findByFragment("50%_");

        // then
        assertEquals(0, result.size());
        verify(patientRepository).searchByFragment("50!%!_");
    }

    @Test
    void findByFragment_escapeCharacter_escapesItFirst() {
        // given
        when(patientRepository.searchByFragment("a!!!%")).thenReturn(List.of());

        // when
        patientService.findByFragment("a!%");

        // then
        verify(patientRepository).searchByFragment("a!!!%");
    }

    @Test
    void create_newEmail_returnsPatientWithEmail() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "jankow@poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(false);
        when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Secret123!")).thenReturn("hash-Secret123!");

        // when
        PatientDto result = patientService.create(command);

        // then
        assertEquals("jankow@poczta.pl", result.email());
        assertEquals("Jan Kowalski", result.fullName());
    }

    @Test
    void create_newEmail_passesPatientWithAccountToSave() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "jankow@poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(false);
        when(patientRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(passwordEncoder.encode("Secret123!")).thenReturn("hash-Secret123!");
        ArgumentCaptor<Patient> captor = ArgumentCaptor.forClass(Patient.class);

        // when
        patientService.create(command);

        // then
        verify(patientRepository).save(captor.capture());
        Patient saved = captor.getValue();
        assertEquals("Jan", saved.getFirstName());
        assertEquals("ABC123456", saved.getIdCardNo());
        assertEquals("jankow@poczta.pl", saved.getUser().getEmail());
        assertEquals("hash-Secret123!", saved.getUser().getPassword());
        assertEquals(Role.PATIENT, saved.getUser().getRole());
    }

    @Test
    void create_emailTaken_throwsUserAlreadyExists() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "jankow@poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(true);

        // when
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> patientService.create(command)
        );

        // then
        assertEquals("User with this email already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void create_emailTakenInOtherCase_throwsUserAlreadyExists() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "JanKow@Poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(true);

        // when
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> patientService.create(command)
        );

        // then
        assertEquals("User with this email already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
    }

    @Test
    void create_idCardNoTaken_throwsPatientAlreadyExistsWithoutNumber() {
        // given
        PatientCreateCommand command = new PatientCreateCommand(
            "jankow@poczta.pl",
            "Secret123!",
            "ABC123456",
            "Jan",
            "Kowalski",
            "123456789",
            LocalDate.of(1990, 5, 15)
        );
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(false);
        when(patientRepository.existsByIdCardNo("ABC123456")).thenReturn(true);

        // when
        PatientAlreadyExistsException exception = assertThrows(
            PatientAlreadyExistsException.class,
            () -> patientService.create(command)
        );

        // then
        assertEquals("Patient with this ID card number already exists", exception.getMessage());
        verify(patientRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void update_patientExists_changesDataAndKeepsIdCardNo() {
        // given
        Patient patient = patientWithAccount("jankow@poczta.pl");
        PatientUpdateCommand command = new PatientUpdateCommand(
            "jankow@poczta.pl",
            "Janusz",
            "Nowak",
            "987654321",
            LocalDate.of(1990, 5, 15)
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // when
        PatientDto result = patientService.update(1L, command);

        // then
        assertEquals("Janusz", result.firstName());
        assertEquals("Nowak", result.lastName());
        assertEquals("ABC123456", patient.getIdCardNo());
        verify(userRepository, never()).existsByEmail(any());
    }

    @Test
    void update_freeEmail_changesAccountEmail() {
        // given
        Patient patient = patientWithAccount("jankow@poczta.pl");
        PatientUpdateCommand command = new PatientUpdateCommand(
            "Janusz.Nowak@Poczta.pl",
            "Janusz",
            "Nowak",
            "987654321",
            LocalDate.of(1990, 5, 15)
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.existsByEmail("janusz.nowak@poczta.pl")).thenReturn(false);

        // when
        PatientDto result = patientService.update(1L, command);

        // then
        assertEquals("janusz.nowak@poczta.pl", result.email());
        assertEquals("janusz.nowak@poczta.pl", patient.getUser().getEmail());
    }

    @Test
    void update_emailOfOtherAccount_throwsUserAlreadyExistsAndKeepsData() {
        // given
        Patient patient = patientWithAccount("jankow@poczta.pl");
        PatientUpdateCommand command = new PatientUpdateCommand(
            "anna.nowak@example.com",
            "Janusz",
            "Nowak",
            "987654321",
            LocalDate.of(1990, 5, 15)
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(userRepository.existsByEmail("anna.nowak@example.com")).thenReturn(true);

        // when
        UserAlreadyExistsException exception = assertThrows(
            UserAlreadyExistsException.class,
            () -> patientService.update(1L, command)
        );

        // then
        assertEquals("User with this email already exists", exception.getMessage());
        assertEquals("Jan", patient.getFirstName());
        assertEquals("jankow@poczta.pl", patient.getUser().getEmail());
    }

    @Test
    void update_patientNotFound_throwsPatientNotFound() {
        // given
        PatientUpdateCommand command = new PatientUpdateCommand(
            "jankow@poczta.pl",
            "Janusz",
            "Nowak",
            "987654321",
            LocalDate.of(1990, 5, 15)
        );
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientService.update(1L, command)
        );

        // then
        assertEquals("Patient with id 1 not found", exception.getMessage());
    }

    @Test
    void changePassword_patientExists_setsNewPassword() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .rawPassword("OldSecret123!")
                    .passwordEncoder(passwordEncoder)
                    .build()
            )
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(passwordEncoder.encode("NewSecret123!")).thenReturn("hash-NewSecret123!");

        // when
        PatientDto result = patientService.changePassword(1L, "NewSecret123!");

        // then
        assertEquals(1L, result.id());
        assertEquals("hash-NewSecret123!", patient.getUser().getPassword());
    }

    @Test
    void changePassword_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientService.changePassword(1L, "NewSecret123!")
        );

        // then
        assertEquals("Patient with id 1 not found", exception.getMessage());
    }

    @Test
    void deleteById_patientWithoutVisits_deletesPatientThenAccount() {
        // given
        User user = User.builder()
            .email("jankow@poczta.pl")
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .build();
        user.setId(7L);
        Patient patient = Patient.builder()
            .id(1L)
            .user(user)
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(1L)).thenReturn(false);
        when(doctorRepository.existsByUserId(7L)).thenReturn(false);

        // when
        patientService.deleteById(1L);

        // then
        InOrder inOrder = inOrder(patientRepository, userRepository);
        inOrder.verify(patientRepository).delete(patient);
        inOrder.verify(userRepository).delete(user);
    }

    @Test
    void deleteById_accountAlsoBelongsToDoctor_deletesPatientKeepsAccount() {
        // given
        User user = User.builder()
            .email("katarzyna.wrobel@example.com")
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .role(Role.DOCTOR)
            .build();
        user.setId(7L);
        Patient patient = Patient.builder()
            .id(1L)
            .user(user)
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(1L)).thenReturn(false);
        when(doctorRepository.existsByUserId(7L)).thenReturn(true);

        // when
        patientService.deleteById(1L);

        // then
        verify(patientRepository).delete(patient);
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_patientHasVisits_throwsPatientHasVisits() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));
        when(visitRepository.existsByPatientId(1L)).thenReturn(true);

        // when
        PatientHasVisitsException exception = assertThrows(
            PatientHasVisitsException.class,
            () -> patientService.deleteById(1L)
        );

        // then
        assertEquals("Patient with id 1 has visits and cannot be deleted", exception.getMessage());
        verify(patientRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
    }

    @Test
    void deleteById_patientNotFound_throwsPatientNotFound() {
        // given
        when(patientRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        PatientNotFoundException exception = assertThrows(
            PatientNotFoundException.class,
            () -> patientService.deleteById(1L)
        );

        // then
        assertEquals("Patient with id 1 not found", exception.getMessage());
        verify(patientRepository, never()).delete(any());
        verify(userRepository, never()).delete(any());
    }

    private Patient patientWithAccount(String email) {
        User user = User.builder()
            .email(email)
            .rawPassword("Secret123!")
            .passwordEncoder(passwordEncoder)
            .build();
        return Patient.builder()
            .id(1L)
            .idCardNo("ABC123456")
            .firstName("Jan")
            .lastName("Kowalski")
            .user(user)
            .build();
    }
}
