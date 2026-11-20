package com.EstateFlowCRM.platform.security.services.Impl;

import com.EstateFlowCRM.platform.security.dto.request.LoginRequest;
import com.EstateFlowCRM.platform.security.dto.request.RegisterRequest;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.security.mapper.AuthMapper;
import com.EstateFlowCRM.platform.security.services.AuthService;
import com.EstateFlowCRM.platform.security.services.JwtService;
import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.user.entities.User;
import com.EstateFlowCRM.platform.user.exceptions.UserAlreadyExistsException;
import com.EstateFlowCRM.platform.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthMapper authMapper;

    @Override
    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        email,
                        request.getPassword()
                )
        );

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        UserDetails userDetails =
                (UserDetails) authentication.getPrincipal();

        String accessToken = jwtService.generateToken(userDetails);


        return authMapper.toLoginResponse(user, accessToken);
    }

    @Override
    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new UserAlreadyExistsException();
        }

        if (request.getRole() != Role.SALES_MANAGER
                && request.getRole() != Role.SALES_AGENT) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "This role cannot be assigned through registration"
            );
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .mustChangePassword(true)
                .build();

        User savedUser = userRepository.save(user);

        return authMapper.toRegisterResponse(savedUser);
    }
}