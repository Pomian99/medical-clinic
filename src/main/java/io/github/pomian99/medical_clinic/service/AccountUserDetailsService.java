package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.model.User;
import io.github.pomian99.medical_clinic.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AccountUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("No account for this email"));

        var account = org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPassword());
        if (user.getRole() != null) {
            account.roles(user.getRole().name());
        }
        return account.build();
    }
}
