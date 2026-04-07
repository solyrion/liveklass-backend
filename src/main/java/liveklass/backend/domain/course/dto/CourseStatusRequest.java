package liveklass.backend.domain.course.dto;

import liveklass.backend.domain.course.entity.CourseStatus;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CourseStatusRequest {

    private CourseStatus status;
}
