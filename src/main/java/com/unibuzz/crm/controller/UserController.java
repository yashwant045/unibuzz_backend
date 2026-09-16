package com.unibuzz.crm.controller;

import com.unibuzz.crm.dto.UserProfileDTO;
import com.unibuzz.crm.model.User;
import com.unibuzz.crm.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.ResponseEntity;
import com.unibuzz.crm.dto.UpdateProfileRequest;
import com.unibuzz.crm.model.Interest;
import com.unibuzz.crm.repository.InterestRepository;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final InterestRepository interestRepository;

    @GetMapping("/profile")
    public UserProfileDTO getProfile(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return UserProfileDTO.builder()
                .fullName(user.getFullName())
                .email(user.getEmail())
                .role(user.getRole())
                .department(user.getDepartment())
                .officeLocation(user.getOfficeLocation())
                .phoneNumber(user.getPhoneNumber())
                .interests(user.getInterests() != null ? user.getInterests().stream().map(Interest::getName).collect(Collectors.toList()) : java.util.Collections.emptyList())
                .build();
    }

    @PutMapping("/profile")
    public ResponseEntity<UserProfileDTO> updateProfile(Authentication authentication, @RequestBody UpdateProfileRequest request) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null) user.setFullName(request.getFullName());
        if (request.getDepartment() != null) user.setDepartment(request.getDepartment());
        if (request.getOfficeLocation() != null) user.setOfficeLocation(request.getOfficeLocation());
        if (request.getPhoneNumber() != null) user.setPhoneNumber(request.getPhoneNumber());
        
        if (request.getInterests() != null) {
            Set<Interest> interestSet = new HashSet<>();
            for (String interestName : request.getInterests()) {
                Interest interest = interestRepository.findByName(interestName)
                        .orElseGet(() -> {
                            Interest newInterest = new Interest();
                            newInterest.setName(interestName);
                            return interestRepository.save(newInterest);
                        });
                interestSet.add(interest);
            }
            user.setInterests(interestSet);
        }

        userRepository.save(user);

        return ResponseEntity.ok(getProfile(authentication));
    }
}
