package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.DoctorCreateCommand;
import io.github.pomian99.medical_clinic.exception.DoctorAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.DoctorHasVisitsException;
import io.github.pomian99.medical_clinic.exception.DoctorNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserNotFoundException;
import io.github.pomian99.medical_clinic.exception.UserRoleConflictException;
import io.github.pomian99.medical_clinic.mapper.DoctorMapper;
import io.github.pomian99.medical_clinic.model.Doctor;
import io.github.pomian99.medical_clinic.model.Role;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import io.github.pomian99.medical_clinic.repository.VisitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {
    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final DoctorMapper mapper;

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public Doctor findById(long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new DoctorNotFoundException(id));
    }

    // Two writes (the doctor and the account's role) must succeed or fail together.
    @Transactional
    public Doctor create(DoctorCreateCommand command) {
        // The account must exist first: reception creates it with POST /users.
        User user = userRepository.findById(command.userId())
                .orElseThrow(() -> new UserNotFoundException(command.userId()));

        // Business Validation: one account can have only one doctor (doctors.user_id is unique).
        if (doctorRepository.existsByUserId(user.getId())) {
            throw new DoctorAlreadyExistsException(user.getId());
        }
        // An account holds a single role, so an account of a patient cannot become a doctor.
        if (user.getRole() != null && user.getRole() != Role.DOCTOR) {
            throw new UserRoleConflictException(user.getId(), user.getRole());
        }

        // An account created without a role becomes a doctor's account now. The user is
        // managed inside the transaction, so the change is saved without calling save().
        user.setRole(Role.DOCTOR);

        Doctor doctor = mapper.toEntity(command);
        doctor.setUser(user);

        return doctorRepository.save(doctor);
    }

    public void delete(long id) {
        if (!doctorRepository.existsById(id)) {
            throw new DoctorNotFoundException(id);
        }
        // A visit cannot exist without a doctor (visits.doctor_id is not null),
        // so a doctor with visits is refused instead of failing on the foreign key.
        if (visitRepository.existsByDoctorId(id)) {
            throw new DoctorHasVisitsException(id);
        }
        doctorRepository.deleteById(id);
    }
}
