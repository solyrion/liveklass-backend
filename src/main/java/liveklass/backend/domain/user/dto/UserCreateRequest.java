package liveklass.backend.domain.user.dto;

import liveklass.backend.domain.user.entity.UserRole;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UserCreateRequest {

    private String name;
    private String email;
    private UserRole role;
}
