package com.unibuzz.crm.service;

import com.unibuzz.crm.dto.RegisterRequest;
import com.unibuzz.crm.model.Faculty;
import com.unibuzz.crm.model.Interest;
import com.unibuzz.crm.model.Role;
import com.unibuzz.crm.model.Student;
import com.unibuzz.crm.model.User;
import com.unibuzz.crm.repository.FacultyRepository;
import com.unibuzz.crm.repository.InterestRepository;
import com.unibuzz.crm.repository.RoleRepository;
import com.unibuzz.crm.repository.StudentRepository;
import com.unibuzz.crm.repository.UserRepository;
import com.unibuzz.crm.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final StudentRepository studentRepository;
    private final FacultyRepository facultyRepository;
    private final InterestRepository interestRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    private static final SecureRandom random = new SecureRandom();
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*[^a-zA-Z0-9]).{6,}$");

    private void validatePassword(String password) {
        if (password == null || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new RuntimeException("Password must be at least 6 characters long and contain at least 1 uppercase letter, 1 lowercase letter, and 1 special character.");
        }
    }

    private String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    @Transactional
    public String register(RegisterRequest request) {

        validatePassword(request.getPassword());

        String email = request.getEmail() != null ? request.getEmail().trim().toLowerCase(Locale.ROOT) : "";

        if (email.isBlank()) {
            throw new RuntimeException("Email is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists! If you haven't verified your email, please try logging in or resend verification code.");
        }

        if (request.getRole() == null || request.getRole().isBlank()) {
            throw new RuntimeException("Role is required");
        }

        String roleInput = request.getRole().trim().toUpperCase(Locale.ROOT);

        if (!roleInput.equals("STUDENT") && !roleInput.equals("FACULTY")) {
            throw new RuntimeException("Invalid role");
        }

        Role role = roleRepository.findByName(roleInput)
                .orElseGet(() -> roleRepository.save(
                        Role.builder().name(roleInput).build()
                ));

        Set<Role> roles = new HashSet<>();
        roles.add(role);

        String otp = generateOtp();

        User user = User.builder()
                .fullName(request.getFullName())
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .emailVerified(false)
                .verificationOtp(otp)
                .otpExpiry(LocalDateTime.now().plusMinutes(15))
                .roles(roles)
                .build();

        userRepository.save(user);

        if (roleInput.equals("STUDENT")) {

            Student student = new Student();
            student.setUser(user);
            student.setEnrollmentNumber(request.getEnrollmentNumber());
            student.setPhoneNumber(request.getPhoneNumber());
            student.setDepartment(request.getDepartment());
            student.setSection(request.getSection());
            student.setYear(request.getYear());

            studentRepository.save(student);

            Set<Interest> interestSet = new HashSet<>();
            List<String> interests = request.getInterests() == null
                    ? List.of()
                    : request.getInterests();

            for (String interestName : interests) {

                Interest interest = interestRepository.findByName(interestName)
                        .orElseGet(() -> {
                            Interest newInterest = new Interest();
                            newInterest.setName(interestName);
                            return interestRepository.save(newInterest);
                        });

                interestSet.add(interest);
            }

            user.setInterests(interestSet);
            userRepository.save(user);
        }

        else {

            Faculty faculty = new Faculty();
            faculty.setUser(user);
            faculty.setDesignation(request.getDesignation());
            faculty.setDepartment(request.getDepartment());
            faculty.setExpertise(request.getExpertise());
            faculty.setOfficeLocation(request.getOfficeLocation());

            facultyRepository.save(faculty);
        }

        // Send OTP verification email
        emailService.sendVerificationOtp(user.getEmail(), otp);

        return "Registration successful! Please check your email for the 6-digit verification code.";
    }

    @Transactional
    public String verifyOtp(String emailInput, String otp) {
        String email = emailInput != null ? emailInput.trim().toLowerCase(Locale.ROOT) : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            return "Email is already verified!";
        }

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid OTP code");
        }

        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP code has expired. Please request a new one.");
        }

        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Email verified successfully! You can now log in.";
    }

    @Transactional
    public String resendOtp(String emailInput) {
        String email = emailInput != null ? emailInput.trim().toLowerCase(Locale.ROOT) : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.isEmailVerified()) {
            throw new RuntimeException("Email is already verified!");
        }

        String newOtp = generateOtp();
        user.setVerificationOtp(newOtp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendVerificationOtp(user.getEmail(), newOtp);

        return "A new 6-digit verification code has been sent to your email.";
    }

    public String login(String emailInput, String password) {
        String email = emailInput != null ? emailInput.trim().toLowerCase(Locale.ROOT) : "";

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        // Only enforce verification for users who have a verification flow configured or are unverified
        if (!user.isEmailVerified() && user.getVerificationOtp() != null) {
            throw new RuntimeException("Please verify your email address before logging in.");
        }

        String role = user.getRoles()
                .stream()
                .findFirst()
                .map(Role::getName)
                .orElse("STUDENT");

        return jwtUtil.generateToken(user.getEmail(), role, user.getFullName());
    }

    @Transactional
    public String forgotPassword(String emailInput) {
        String email = emailInput != null ? emailInput.trim().toLowerCase(Locale.ROOT) : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address."));

        String otp = generateOtp();
        user.setVerificationOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        emailService.sendPasswordResetOtp(user.getEmail(), otp);

        return "A 6-digit password reset code has been sent to your email address.";
    }

    @Transactional
    public String resetPassword(String emailInput, String otp, String newPassword) {
        String email = emailInput != null ? emailInput.trim().toLowerCase(Locale.ROOT) : "";
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("No account found with this email address."));

        if (user.getVerificationOtp() == null || !user.getVerificationOtp().equals(otp.trim())) {
            throw new RuntimeException("Invalid OTP code");
        }

        if (user.getOtpExpiry() != null && user.getOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP code has expired. Please request a new one.");
        }

        validatePassword(newPassword);

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEmailVerified(true);
        user.setVerificationOtp(null);
        user.setOtpExpiry(null);
        userRepository.save(user);

        return "Password reset successfully! You can now log in with your new password.";
    }
}
