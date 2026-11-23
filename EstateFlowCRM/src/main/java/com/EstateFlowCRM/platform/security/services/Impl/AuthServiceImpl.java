package com.EstateFlowCRM.platform.security.services.Impl;

import com.EstateFlowCRM.platform.security.dto.request.LoginRequest;
import com.EstateFlowCRM.platform.security.dto.request.RegisterRequest;
import com.EstateFlowCRM.platform.security.dto.response.LoginResponse;
import com.EstateFlowCRM.platform.security.dto.response.RegisterResponse;
import com.EstateFlowCRM.platform.security.mapper.AuthMapper;
import com.EstateFlowCRM.platform.security.services.AuthService;
import com.EstateFlowCRM.platform.security.services.JwtService;
import com.EstateFlowCRM.platform.security.exceptions.InvalidCredentialsException;
import com.EstateFlowCRM.platform.security.exceptions.RoleAssignmentNotAllowedException;
import com.EstateFlowCRM.platform.user.entities.Role;
import com.EstateFlowCRM.platform.user.entities.User;
import com.EstateFlowCRM.platform.user.exceptions.UserAlreadyExistsException;
import com.EstateFlowCRM.platform.user.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.Locale;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Pattern EMAIL_UNIQUE_VIOLATION_DETAIL =
            Pattern.compile("(?i)\\bKey\\s*\\(\\s*email\\s*\\)\\s*=");

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

        Authentication authentication;
        try {
            authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(email, request.getPassword())
            );
        } catch (AuthenticationException exception) {
            throw new InvalidCredentialsException();
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user was not found in the user repository"
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
            throw new RoleAssignmentNotAllowedException();
        }

        User user = User.builder()
                .fullName(request.getFullName().trim())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .enabled(true)
                .mustChangePassword(true)
                .build();

        User savedUser;
        try {
            // Flush now so a concurrent duplicate email is reported inside this method.
            savedUser = userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) { // general exception
            if (isEmailUniqueConstraintViolation(exception)) {  // if from email conflict
                throw new UserAlreadyExistsException( // not general exception
                        "User with this email already exists",
                        exception
                );
            }
            throw exception; // if not from email conflict
        }

        return authMapper.toRegisterResponse(savedUser);
    }

    // هل المشكلة اللي صارت تحديدًا بسبب انتهاك Unique Constraint الخاص بحقل Email؟
    private boolean isEmailUniqueConstraintViolation(Throwable exception) { // استثناءات مغلفه بداخل بعضها
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException constraintViolation
                    && User.EMAIL_UNIQUE_CONSTRAINT.equalsIgnoreCase(
                            constraintViolation.getConstraintName()
                    )) {
                return true;
            }

            if (cause instanceof SQLException sqlException
                    && hasEmailUniqueViolationDetail(sqlException)) {
                return true;
            }
        }
        return false;
    }

    // احتياط
    private boolean hasEmailUniqueViolationDetail(SQLException exception) {
        for (SQLException sqlException = exception;
             sqlException != null;
             sqlException = sqlException.getNextException()) { // don't layer, just sqlException layer but for navigate between sqlExceptions
            if ("23505".equals(sqlException.getSQLState())
                    && sqlException.getMessage() != null
                    && EMAIL_UNIQUE_VIOLATION_DETAIL.matcher(sqlException.getMessage()).find()) {
                return true;
            }
        }
        return false;
    }
}
