package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.DoctorCreateCommand;
import io.github.pomian99.medical_clinic.dto.DoctorDto;
import io.github.pomian99.medical_clinic.exception.DoctorAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.DoctorHasVisitsException;
import io.github.pomian99.medical_clinic.exception.DoctorNotFoundException;
import io.github.pomian99.medical_clinic.exception.FacilityNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserRoleConflictException;
import io.github.pomian99.medical_clinic.mapper.DoctorMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Facility;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.Specialization;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.FacilityRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
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
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DoctorServiceTest {

    @Mock
    DoctorRepository doctorRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    VisitRepository visitRepository;

    @Mock
    FacilityRepository facilityRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    DoctorService doctorService;

    @BeforeEach
    void setUp() {
        DoctorMapper mapper = Mappers.getMapper(DoctorMapper.class);
        doctorService = new DoctorService(doctorRepository, userRepository, visitRepository, facilityRepository, mapper);
    }

    @Test
    void findAll_twoDoctors_returnsBoth() {
        // given
        List<Doctor> doctors = List.of(
            doctor(1L, "Katarzyna", account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR)),
            doctor(2L, "Tomasz", account(11L, "tomasz.mazur@example.com", Role.DOCTOR))
        );
        when(doctorRepository.findAll()).thenReturn(doctors);

        // when
        List<DoctorDto> result = doctorService.findAll();

        // then
        assertEquals(List.of("Katarzyna", "Tomasz"), result.stream().map(DoctorDto::firstName).toList());
    }

    @Test
    void findById_doctorExists_returnsAccountAndFacilitiesSortedByName() {
        // given
        Doctor doctor = doctor(1L, "Katarzyna", account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR));
        doctor.addFacility(facility(2L, "Przychodnia Podgórze"));
        doctor.addFacility(facility(1L, "Przychodnia Krowodrza"));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));

        // when
        DoctorDto result = doctorService.findById(1L);

        // then
        assertEquals(10L, result.userId());
        assertEquals("katarzyna.wrobel@example.com", result.email());
        assertEquals(List.of("Przychodnia Krowodrza", "Przychodnia Podgórze"), result.facilities());
    }

    @Test
    void findById_doctorNotFound_throwsDoctorNotFound() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(
            DoctorNotFoundException.class,
            () -> doctorService.findById(1L)
        );

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
    }

    @Test
    void create_accountWithoutRole_savesDoctorAndGivesAccountDoctorRole() {
        // given
        User user = account(10L, "adam.lis@example.com", null);
        DoctorCreateCommand command = new DoctorCreateCommand(10L, "Adam", "Lis", Specialization.CARDIOLOGIST);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(10L)).thenReturn(false);
        when(doctorRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        ArgumentCaptor<Doctor> captor = ArgumentCaptor.forClass(Doctor.class);

        // when
        DoctorDto result = doctorService.create(command);

        // then
        verify(doctorRepository).save(captor.capture());
        Doctor saved = captor.getValue();
        assertSame(user, saved.getUser());
        assertEquals("Adam", saved.getFirstName());
        assertEquals(Specialization.CARDIOLOGIST, saved.getSpecialization());
        assertEquals(Role.DOCTOR, user.getRole());
        assertEquals("adam.lis@example.com", result.email());
    }

    @Test
    void create_accountNotFound_throwsUserNotFound() {
        // given
        DoctorCreateCommand command = new DoctorCreateCommand(10L, "Adam", "Lis", Specialization.CARDIOLOGIST);
        when(userRepository.findById(10L)).thenReturn(Optional.empty());

        // when
        UserNotFoundException exception = assertThrows(
            UserNotFoundException.class,
            () -> doctorService.create(command)
        );

        // then
        assertEquals("User with id 10 not found", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_accountAlreadyHasDoctor_throwsDoctorAlreadyExists() {
        // given
        User user = account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR);
        DoctorCreateCommand command = new DoctorCreateCommand(10L, "Katarzyna", "Wróbel", Specialization.CARDIOLOGIST);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(10L)).thenReturn(true);

        // when
        DoctorAlreadyExistsException exception = assertThrows(
            DoctorAlreadyExistsException.class,
            () -> doctorService.create(command)
        );

        // then
        assertEquals("User with id 10 already has a doctor", exception.getMessage());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void create_accountOfPatient_throwsUserRoleConflictAndKeepsRole() {
        // given
        User user = account(10L, "anna.nowak@example.com", Role.PATIENT);
        DoctorCreateCommand command = new DoctorCreateCommand(10L, "Anna", "Nowak", Specialization.PEDIATRICIAN);
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));
        when(doctorRepository.existsByUserId(10L)).thenReturn(false);

        // when
        UserRoleConflictException exception = assertThrows(
            UserRoleConflictException.class,
            () -> doctorService.create(command)
        );

        // then
        assertEquals("User with id 10 already has role PATIENT", exception.getMessage());
        assertEquals(Role.PATIENT, user.getRole());
        verify(doctorRepository, never()).save(any());
    }

    @Test
    void delete_doctorWithoutVisits_deletesDoctor() {
        // given
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.existsByDoctorId(1L)).thenReturn(false);

        // when
        doctorService.delete(1L);

        // then
        verify(doctorRepository).deleteById(1L);
    }

    @Test
    void delete_doctorNotFound_throwsDoctorNotFound() {
        // given
        when(doctorRepository.existsById(1L)).thenReturn(false);

        // when
        DoctorNotFoundException exception = assertThrows(
            DoctorNotFoundException.class,
            () -> doctorService.delete(1L)
        );

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(doctorRepository, never()).deleteById(anyLong());
    }

    @Test
    void delete_doctorHasVisits_throwsDoctorHasVisits() {
        // given
        when(doctorRepository.existsById(1L)).thenReturn(true);
        when(visitRepository.existsByDoctorId(1L)).thenReturn(true);

        // when
        DoctorHasVisitsException exception = assertThrows(
            DoctorHasVisitsException.class,
            () -> doctorService.delete(1L)
        );

        // then
        assertEquals("Doctor with id 1 has visits and cannot be deleted", exception.getMessage());
        verify(doctorRepository, never()).deleteById(anyLong());
    }

    @Test
    void addFacility_doctorAndFacilityExist_assignsFacility() {
        // given
        Doctor doctor = doctor(1L, "Katarzyna", account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR));
        Facility facility = facility(2L, "Przychodnia Podgórze");
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(facility));

        // when
        DoctorDto result = doctorService.addFacility(1L, 2L);

        // then
        assertTrue(doctor.getFacilities().contains(facility));
        assertEquals(List.of("Przychodnia Podgórze"), result.facilities());
    }

    @Test
    void addFacility_facilityNotFound_throwsFacilityNotFoundAndAssignsNothing() {
        // given
        Doctor doctor = doctor(1L, "Katarzyna", account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR));
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(9L)).thenReturn(Optional.empty());

        // when
        FacilityNotFoundException exception = assertThrows(
            FacilityNotFoundException.class,
            () -> doctorService.addFacility(1L, 9L)
        );

        // then
        assertEquals("Facility with id 9 not found", exception.getMessage());
        assertTrue(doctor.getFacilities().isEmpty());
    }

    @Test
    void addFacility_doctorNotFound_throwsDoctorNotFound() {
        // given
        when(doctorRepository.findById(1L)).thenReturn(Optional.empty());

        // when
        DoctorNotFoundException exception = assertThrows(
            DoctorNotFoundException.class,
            () -> doctorService.addFacility(1L, 2L)
        );

        // then
        assertEquals("Doctor with id 1 not found", exception.getMessage());
        verify(facilityRepository, never()).findById(anyLong());
    }

    @Test
    void removeFacility_facilityAssigned_removesOnlyTheAssignment() {
        // given
        Doctor doctor = doctor(1L, "Katarzyna", account(10L, "katarzyna.wrobel@example.com", Role.DOCTOR));
        Facility krowodrza = facility(1L, "Przychodnia Krowodrza");
        Facility podgorze = facility(2L, "Przychodnia Podgórze");
        doctor.addFacility(krowodrza);
        doctor.addFacility(podgorze);
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(facilityRepository.findById(2L)).thenReturn(Optional.of(podgorze));

        // when
        DoctorDto result = doctorService.removeFacility(1L, 2L);

        // then
        assertEquals(List.of("Przychodnia Krowodrza"), result.facilities());
        verify(facilityRepository, never()).delete(any());
    }

    private Doctor doctor(long id, String firstName, User user) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setFirstName(firstName);
        doctor.setLastName("Test");
        doctor.setSpecialization(Specialization.CARDIOLOGIST);
        doctor.setUser(user);
        return doctor;
    }

    private User account(long id, String email, Role role) {
        User user = User.builder()
            .email(email)
            .rawPassword("Secret#123")
            .passwordEncoder(passwordEncoder)
            .role(role)
            .build();
        user.setId(id);
        return user;
    }

    private static Facility facility(long id, String name) {
        return new Facility(id, name, "Kraków", null, null, null);
    }
}
