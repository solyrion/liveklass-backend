package liveklass.backend.domain.user.service;

import liveklass.backend.domain.user.dto.UserCreateRequest;
import liveklass.backend.domain.user.dto.UserResponse;
import liveklass.backend.domain.user.entity.User;
import liveklass.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(UserCreateRequest request) {
        User user = User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .role(request.getRole())
                .build();

        return UserResponse.from(userRepository.save(user));
    }
}
