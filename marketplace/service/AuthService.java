package com.example.marketplace.service;

import com.example.marketplace.dto.AuthResponse;
import com.example.marketplace.dto.LoginRequest;
import com.example.marketplace.dto.RegisterRequest;
import com.example.marketplace.dto.UserDto;
import com.example.marketplace.model.User;
import com.example.marketplace.repository.UserRepository;
import com.example.marketplace.util.JwtUtil;
import com.example.marketplace.util.PasswordUtil;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.regex.Pattern;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");
    private static final Pattern PHONE_PATTERN = Pattern.compile("^(\\+?\\d{6,20}|(\\d{3,4}-)?\\d{7,8})$");

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public ResponseEntity<?> register(RegisterRequest request) {
        if (!StringUtils.hasText(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "用户名不能为空"));
        }

        if (!StringUtils.hasText(request.getPassword()) || request.getPassword().length() < 6) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "密码至少需要6位字符"));
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "用户名已存在"));
        }
        
        if (StringUtils.hasText(request.getEmail())) {
            if (!EMAIL_PATTERN.matcher(request.getEmail()).matches()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "邮箱格式不正确"));
            }
            if (userRepository.existsByEmail(request.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "邮箱已被使用"));
            }
        }

        if (StringUtils.hasText(request.getPhone()) && !PHONE_PATTERN.matcher(request.getPhone()).matches()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", "联系电话格式不正确"));
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(PasswordUtil.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("USER");

        user = userRepository.save(user);

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        UserDto userDto = toUserDto(user);

        return ResponseEntity.ok(new AuthResponse(token, userDto));
    }

    public ResponseEntity<?> login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
            .orElse(null);

        if (user == null || !PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "用户名或密码错误"));
        }

        String token = JwtUtil.generateToken(user.getId(), user.getUsername());
        UserDto userDto = toUserDto(user);

        return ResponseEntity.ok(new AuthResponse(token, userDto));
    }

    public UserDto getUserById(Long userId) {
        return userRepository.findById(userId)
            .map(this::toUserDto)
            .orElse(null);
    }

    public ResponseEntity<?> updateUser(Long userId, String email, String phone) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(Map.of("error", "用户不存在"));
        }

        if (StringUtils.hasText(email)) {
            if (!EMAIL_PATTERN.matcher(email).matches()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "邮箱格式不正确"));
            }
            if (userRepository.existsByEmail(email) && !email.equalsIgnoreCase(user.getEmail())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "邮箱已被使用"));
            }
            user.setEmail(email);
        } else if (email != null && email.isEmpty()) {
            user.setEmail(null);
        }

        if (phone != null) {
            if (StringUtils.hasText(phone)) {
                if (!PHONE_PATTERN.matcher(phone).matches()) {
                    return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(Map.of("error", "联系电话格式不正确"));
                }
                user.setPhone(phone);
            } else {
                user.setPhone(null);
            }
        }

        userRepository.save(user);
        return ResponseEntity.ok(toUserDto(user));
    }

    private UserDto toUserDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole());
        return dto;
    }
}

