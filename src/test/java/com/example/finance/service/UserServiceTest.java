package com.example.finance.service;

import com.example.finance.dto.AuthRegisterRequest;
import com.example.finance.entity.UserEntity;
import com.example.finance.exception.ConflictException;
import com.example.finance.exception.NotFoundException;
import com.example.finance.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void register_success() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setUsername("test@example.com");
        request.setPassword("password");
        request.setFullName("Test User");
        request.setPhoneNumber("1234567890");

        when(userRepository.existsByUsername("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password")).thenReturn("encodedPassword");
        
        UserEntity savedUser = new UserEntity();
        savedUser.setId(1L);
        savedUser.setUsername("test@example.com");
        savedUser.setPassword("encodedPassword");
        savedUser.setFullName("Test User");
        savedUser.setPhoneNumber("1234567890");
        
        when(userRepository.save(any(UserEntity.class))).thenReturn(savedUser);

        UserEntity result = userService.register(request);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getUsername());
        assertEquals("encodedPassword", result.getPassword());
        assertEquals("Test User", result.getFullName());
        assertEquals("1234567890", result.getPhoneNumber());
        
        verify(userRepository).existsByUsername("test@example.com");
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(UserEntity.class));
    }

    @Test
    void register_usernameExists_throwsConflictException() {
        AuthRegisterRequest request = new AuthRegisterRequest();
        request.setUsername("test@example.com");

        when(userRepository.existsByUsername("test@example.com")).thenReturn(true);

        assertThrows(ConflictException.class, () -> userService.register(request));
        
        verify(userRepository).existsByUsername("test@example.com");
        verify(userRepository, never()).save(any(UserEntity.class));
    }

    @Test
    void getCurrentUser_success() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");

        UserEntity user = new UserEntity();
        user.setId(1L);
        user.setUsername("test@example.com");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.of(user));

        UserEntity result = userService.getCurrentUser();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("test@example.com", result.getUsername());
        
        verify(userRepository).findByUsername("test@example.com");
    }

    @Test
    void getCurrentUser_noAuth_throwsNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_nameNull_throwsNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn(null);

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
    }

    @Test
    void getCurrentUser_userNotFoundInDb_throwsNotFoundException() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("test@example.com");
        when(userRepository.findByUsername("test@example.com")).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> userService.getCurrentUser());
    }
}
