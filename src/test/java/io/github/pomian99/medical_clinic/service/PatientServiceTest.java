package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.model.Patient;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock
    PatientRepository patientRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    PatientMapper mapper;

    @InjectMocks
    PatientService patientService;

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
        Patient result = patientService.findById(1L);

        // then
        assertEquals("Jan", result.getFirstName());
    }

    @Test
    void findByEmail_patientExists_returnsPatient() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .build()
            )
            .build();
        when(patientRepository.findByUserEmail("jankow@poczta.pl")).thenReturn(Optional.of(patient));

        // when
        Patient result = patientService.findByEmail("jankow@poczta.pl");

        // then
        assertEquals(1L, result.getId());
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
        List<Patient> result = patientService.findAll();

        // then
        assertEquals(2, result.size());
        assertEquals("Anna", result.get(1).getFirstName());
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
        List<Patient> result = patientService.findByFragment("Kowal");

        // then
        assertEquals(1, result.size());
        assertEquals("Kowalski", result.get(0).getLastName());
    }

    @Test
    void create_newEmail_savesPatientWithAccount() {
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
        Patient patient = Patient.builder()
            .firstName("Jan")
            .lastName("Kowalski")
            .build();
        when(userRepository.existsByEmail("jankow@poczta.pl")).thenReturn(false);
        when(mapper.toEntity(command)).thenReturn(patient);
        when(patientRepository.save(patient)).thenReturn(patient);

        // when
        Patient result = patientService.create(command);

        // then
        assertEquals("jankow@poczta.pl", result.getUser().getEmail());
        assertEquals("Secret123!", result.getUser().getPassword());
        assertEquals(Role.PATIENT, result.getUser().getRole());
    }

    @Test
    void changePassword_patientExists_setsNewPassword() {
        // given
        Patient patient = Patient.builder()
            .id(1L)
            .user(
                User.builder()
                    .email("jankow@poczta.pl")
                    .password("OldSecret123!")
                    .build()
            )
            .build();
        when(patientRepository.findById(1L)).thenReturn(Optional.of(patient));

        // when
        Patient result = patientService.changePassword(1L, "NewSecret123!");

        // then
        assertEquals("NewSecret123!", result.getUser().getPassword());
    }
}
