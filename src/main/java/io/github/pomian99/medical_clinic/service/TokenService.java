package io.github.pomian99.medical_clinic.service;

import io.github.pomian99.medical_clinic.config.JwtConfig;
import io.github.pomian99.medical_clinic.dto.LoginCommand;
import io.github.pomian99.medical_clinic.dto.TokenDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenService {
    static final long EXPIRES_IN_SECONDS = 3600;
    private static final String TOKEN_TYPE = "Bearer";
    private static final String ROLE_PREFIX = "ROLE_";

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final Clock clock;

    public TokenDto issue(LoginCommand command) {
        Authentication authentication = authenticationManager.authenticate(
            UsernamePasswordAuthenticationToken.unauthenticated(command.email(), command.password())
        );

        Instant issuedAt = clock.instant();
        JwtClaimsSet claims = JwtClaimsSet.builder()
            .issuer(JwtConfig.ISSUER)
            .subject(authentication.getName())
            .issuedAt(issuedAt)
            .expiresAt(issuedAt.plusSeconds(EXPIRES_IN_SECONDS))
            .claim("roles", roles(authentication))
            .build();
        JwsHeader header = JwsHeader.with(SignatureAlgorithm.RS256).build();
        String token = jwtEncoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();

        log.info("Issued access token valid for {} s", EXPIRES_IN_SECONDS);
        return new TokenDto(token, TOKEN_TYPE, EXPIRES_IN_SECONDS);
    }

    private static List<String> roles(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .map(GrantedAuthority::getAuthority)
            .filter(authority -> authority.startsWith(ROLE_PREFIX))
            .map(authority -> authority.substring(ROLE_PREFIX.length()))
            .toList();
    }
}
