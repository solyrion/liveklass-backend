package liveklass.backend.domain.course.dto;

import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.course.entity.CourseStatus;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
public class CourseResponse {

    private Long id;
    private Long creatorId;
    private String creatorName;
    private String title;
    private String description;
    private int price;
    private int capacity;
    private int enrolledCount;
    private int remainingCount;
    private LocalDate startDate;
    private LocalDate endDate;
    private CourseStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static CourseResponse from(Course course) {
        return CourseResponse.builder()
                .id(course.getId())
                .creatorId(course.getCreator().getId())
                .creatorName(course.getCreator().getName())
                .title(course.getTitle())
                .description(course.getDescription())
                .price(course.getPrice())
                .capacity(course.getCapacity())
                .enrolledCount(course.getEnrolledCount())
                .remainingCount(course.getCapacity() - course.getEnrolledCount())
                .startDate(course.getStartDate())
                .endDate(course.getEndDate())
                .status(course.getStatus())
                .createdAt(course.getCreatedAt())
                .updatedAt(course.getUpdatedAt())
                .build();
    }
}
