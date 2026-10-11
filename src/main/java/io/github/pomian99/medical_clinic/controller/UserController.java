package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.mapper.UserMapper;
import io.github.pomian99.medical_clinic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Users", description = "Medical clinic accounts (login data and role): create, read")
@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final UserMapper mapper;

    @Operation(summary = "List all users")
    @GetMapping
    public List<UserDto> getUsers() {
        return mapper.toDto(userService.findAll());
    }

    @Operation(summary = "Find user with given email")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(
                    responseCode = "404",
                    description = "No user with this email exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping(params = "email")
    public UserDto getUserByEmail(@RequestParam String email) {
        return mapper.toDto(userService.findByEmail(email));
    }

    @Operation(summary = "Find user with given id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No user with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping(path = "/{id}")
    public UserDto getUserById(@PathVariable long id) {
        return mapper.toDto(userService.findById(id));
    }

    @Operation(
            summary = "Create new user",
            description = "Creates an account on its own, without a patient attached. "
                    + "The response never contains the password."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON "
                            + "(an unknown role also makes it unreadable)",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "This email already belongs to an account",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto createUser(@Valid @RequestBody UserCreateCommand command) {
        return mapper.toDto(userService.create(command));
    }

    @Operation(
            summary = "Delete user with given id",
            description = "Only an account that no patient points to can be deleted."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "User deleted"),
            @ApiResponse(
                    responseCode = "400",
                    description = "The id in the path is not a number",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "No user with this id exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    )),
            @ApiResponse(
                    responseCode = "409",
                    description = "The account belongs to a patient, so it cannot be deleted",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @DeleteMapping(path = "/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUserById(@PathVariable long id) {
        userService.deleteById(id);
    }
}
