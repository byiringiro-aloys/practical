package com.erp.Enterprise_Resource_Planning.service;

import com.erp.Enterprise_Resource_Planning.config.JwtUtil;
import com.erp.Enterprise_Resource_Planning.dto.AuthResponse;
import com.erp.Enterprise_Resource_Planning.dto.ChangePasswordRequest;
import com.erp.Enterprise_Resource_Planning.dto.LoginRequest;
import com.erp.Enterprise_Resource_Planning.dto.RegisterRequest;
import com.erp.Enterprise_Resource_Planning.entity.Role;
import com.erp.Enterprise_Resource_Planning.entity.User;
import com.erp.Enterprise_Resource_Planning.exception.BadRequestException;
import com.erp.Enterprise_Resource_Planning.exception.DuplicateResourceException;
import com.erp.Enterprise_Resource_Planning.exception.ResourceNotFoundException;
import com.erp.Enterprise_Resource_Planning.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       AuthenticationManager authenticationManager,
                       JwtUtil jwtUtil,
                       UserDetailsService userDetailsService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    // ── Login (all roles) ──────────────────────────────────────────────────

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        UserDetails ud = userDetailsService.loadUserByUsername(request.getEmail());
        String token = jwtUtil.generateToken(ud);
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    // ── Register ADMIN / MANAGER accounts (ADMIN only) ────────────────────

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        // EMPLOYEE accounts are created automatically via EmployeeService.
        // This endpoint is strictly for privileged accounts.
        if (request.getRole() == Role.EMPLOYEE) {
            throw new BadRequestException(
                "Cannot register an EMPLOYEE account through this endpoint. " +
                "Add the employee via POST /api/employees instead."
            );
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .build();
        userRepository.save(user);

        UserDetails ud = userDetailsService.loadUserByUsername(user.getEmail());
        String token = jwtUtil.generateToken(ud);
        return new AuthResponse(token, user.getEmail(), user.getRole().name());
    }

    // ── Change own password (all authenticated users) ─────────────────────

    @Transactional
    public void changePassword(String email, ChangePasswordRequest request) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadCredentialsException("Current password is incorrect.");
        }

        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException("New password must differ from the current password.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}
