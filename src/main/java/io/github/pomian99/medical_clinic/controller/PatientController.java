package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.EditPasswordCommand;
import io.github.pomian99.medical_clinic.dto.PatientCreateCommand;
import io.github.pomian99.medical_clinic.dto.PatientDto;
import io.github.pomian99.medical_clinic.dto.PatientUpdateCommand;
import io.github.pomian99.medical_clinic.mapper.PatientMapper;
import io.github.pomian99.medical_clinic.service.PatientService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/patients")
@RequiredArgsConstructor
public class PatientController {
    private final PatientService patientService;
    private final PatientMapper mapper;

    @GetMapping
    public List<PatientDto> getPatients() {
        return mapper.toDto(patientService.findAll());
    }

    @GetMapping(params = "email")
    public PatientDto getPatientByEmail(@RequestParam String email) {
        return mapper.toDto(patientService.findByEmail(email));
    }

    @GetMapping("/{id}")
    public PatientDto getPatientById(@PathVariable long id) {
        return mapper.toDto(patientService.findById(id));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PatientDto createPatient(@Valid @RequestBody PatientCreateCommand command) {
        return mapper.toDto(patientService.create(command));
    }

    @PutMapping("/{id}")
    public PatientDto updatePatient(
            @PathVariable long id,
            @Valid @RequestBody PatientUpdateCommand command
    ) {
        return mapper.toDto(patientService.update(id, command));
    }

    @PatchMapping("/{id}/password")
    public PatientDto updatePatientPassword(
            @PathVariable long id,
            @Valid @RequestBody EditPasswordCommand command
            ) {
        return mapper.toDto(patientService.updatePassword(id, command.password()));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deletePatient(@PathVariable long id) {
        patientService.delete(id);
    }
}
