package liveklass.backend.domain.course.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor
public class CourseCreateRequest {

    private String title;
    private String description;
    private int price;
    private int capacity;
    private LocalDate startDate;
    private LocalDate endDate;
}
