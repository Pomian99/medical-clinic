package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.exception.UserAlreadyExistsException;
import io.github.pomian99.medical_clinic.exception.UserInUseException;
import io.github.pomian99.medical_clinic.exception.UserNotFoundException;
import io.github.pomian99.medical_clinic.mapper.UserMapper;
import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.DoctorRepository;
import io.github.pomian99.medical_clinic.repository.PatientRepository;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final UserMapper mapper;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return mapper.toDto(userRepository.findAll());
    }

    @Transactional(readOnly = true)
    public UserDto findById(long id) {
        User user = userRepository.findById(id)
            .orElseThrow(() -> new UserNotFoundException(id));
        return mapper.toDto(user);
    }

    @Transactional(readOnly = true)
    public UserDto findByEmail(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(UserNotFoundException::new);
        return mapper.toDto(user);
    }

    @Transactional
    public UserDto create(UserCreateCommand command) {
        // Business Validation: this rule requires checking other users,
        // so it cannot be handled by field-level validation annotations.
        if (userRepository.existsByEmail(command.email())) {
            throw new UserAlreadyExistsException();
        }

        User user = User.builder()
            .email(command.email())
            .rawPassword(command.password())
            .passwordEncoder(passwordEncoder)
            .role(command.role())
            .build();

        User saved = userRepository.save(user);
        log.info("Created user {}", saved.getId());
        return mapper.toDto(saved);
    }

    @Transactional
    public void deleteById(long id) {
        if (!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }
        if (patientRepository.existsByUserId(id) || doctorRepository.existsByUserId(id)) {
            throw new UserInUseException(id);
        }
        userRepository.deleteById(id);
        log.info("Deleted user {}", id);
    }
}
