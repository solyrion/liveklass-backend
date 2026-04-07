package liveklass.backend.domain.enrollment.dto;

import liveklass.backend.domain.enrollment.entity.Enrollment;
import liveklass.backend.domain.enrollment.entity.EnrollmentStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EnrollmentResponse {

    private Long id;
    private Long courseId;
    private String courseTitle;
    private Long userId;
    private EnrollmentStatus status;
    private LocalDateTime confirmedAt;
    private LocalDateTime cancelledAt;
    private LocalDateTime createdAt;

    public static EnrollmentResponse from(Enrollment enrollment) {
        return EnrollmentResponse.builder()
                .id(enrollment.getId())
                .courseId(enrollment.getCourse().getId())
                .courseTitle(enrollment.getCourse().getTitle())
                .userId(enrollment.getUser().getId())
                .status(enrollment.getStatus())
                .confirmedAt(enrollment.getConfirmedAt())
                .cancelledAt(enrollment.getCancelledAt())
                .createdAt(enrollment.getCreatedAt())
                .build();
    }
}
