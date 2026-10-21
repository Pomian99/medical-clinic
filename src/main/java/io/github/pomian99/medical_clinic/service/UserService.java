package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.UserInUseException;
import io.github.pomian99.medical_clinic.exception.UserNotFoundException;
import io.github.pomian99.medical_clinic.mapper.UserMapper;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserMapper mapper;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(long id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
    }

    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
            .orElseThrow(() -> new UserNotFoundException(email));
    }

    public User create(UserCreateCommand command) {
        // Business Validation: this rule requires checking other users,
        // so it cannot be handled by field-level validation annotations.
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException(command.email());
        }

        return userRepository.save(mapper.toEntity(command));
    }

    public void deleteById(long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        if (patientRepository.existsByUserId(id) || doctorRepository.existsByUserId(id)) {
            throw new UserInUseException(id);
        }
        userRepository.deleteById(id);
    }
}
