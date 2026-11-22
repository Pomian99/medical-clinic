package io.github.pomian99.medical_clinic.controller;

import io.github.pomian99.medical_clinic.dto.UserCreateCommand;
import io.github.pomian99.medical_clinic.dto.UserDto;
import io.github.pomian99.medical_clinic.dto.UserUpdateCommand;
import io.github.pomian99.medical_clinic.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
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
import org.springframework.web.bind.annotation.PutMapping;
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

    @Operation(
            summary = "List users, or find one by email",
            description = "One path with two variants, chosen by the query parameter:\n\n"
                    + "- no parameter: all accounts;\n"
                    + "- `email`: the account with this email, letter case ignored; one account, not a list."
                    + "\n\nThe response never contains the password.",
            parameters = @Parameter(
                    name = "email",
                    in = ParameterIn.QUERY,
                    required = false,
                    description = "Email of the account; the response is one account instead of a list",
                    schema = @Schema(type = "string")
            )
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "A list of accounts, or one account when email is given",
                    content = @Content(
                            schema = @Schema(oneOf = {UserDto[].class, UserDto.class})
                    )),
            @ApiResponse(
                    responseCode = "404",
                    description = "Only with email: no account with this email exists",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @GetMapping
    public List<UserDto> getUsers() {
        return userService.findAll();
    }

    @Operation(hidden = true)
    @GetMapping(params = "email")
    public UserDto getUserByEmail(@RequestParam String email) {
        return userService.findByEmail(email);
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
        return userService.findById(id);
    }

    @Operation(
            summary = "Create new user",
            description = "Creates an account on its own, without a patient attached. "
                    + "The role is optional: an account without one gets DOCTOR when "
                    + "a doctor is created on it. The response never contains the password."
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
        return userService.create(command);
    }

    @Operation(
            summary = "Update user with given id",
            description = "Changes the email of the account; the patient or doctor on it keeps "
                    + "using the new one. The role follows the patient or doctor attached to the "
                    + "account, so it is not changed here."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(
                    responseCode = "400",
                    description = "Request body failed validation or is not valid JSON, "
                            + "or the id in the path is not a number",
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
                    description = "This email belongs to another account",
                    content = @Content(
                            schema = @Schema(implementation = ProblemDetail.class)
                    ))
    })
    @PutMapping(path = "/{id}")
    public UserDto updateUser(@PathVariable long id, @Valid @RequestBody UserUpdateCommand command) {
        return userService.update(id, command);
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
