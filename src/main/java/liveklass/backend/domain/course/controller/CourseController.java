package liveklass.backend.domain.course.controller;

import liveklass.backend.domain.course.dto.CourseCreateRequest;
import liveklass.backend.domain.course.dto.CourseResponse;
import liveklass.backend.domain.course.dto.CourseStatusRequest;
import liveklass.backend.domain.course.entity.CourseStatus;
import liveklass.backend.domain.course.service.CourseService;
import liveklass.backend.domain.enrollment.dto.EnrollmentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CourseResponse createCourse(
            @RequestHeader("X-User-Id") Long creatorId,
            @RequestBody CourseCreateRequest request) {
        return courseService.createCourse(creatorId, request);
    }

    @PatchMapping("/{courseId}/status")
    public CourseResponse changeStatus(
            @RequestHeader("X-User-Id") Long creatorId,
            @PathVariable Long courseId,
            @RequestBody CourseStatusRequest request) {
        return courseService.changeStatus(creatorId, courseId, request);
    }

    @GetMapping
    public Page<CourseResponse> getCourses(
            @RequestParam(required = false) CourseStatus status,
            @PageableDefault(size = 20) Pageable pageable) {
        return courseService.getCourses(status, pageable);
    }

    @GetMapping("/{courseId}")
    public CourseResponse getCourse(@PathVariable Long courseId) {
        return courseService.getCourse(courseId);
    }

    @GetMapping("/{courseId}/enrollments")
    public Page<EnrollmentResponse> getCourseEnrollments(
            @RequestHeader("X-User-Id") Long creatorId,
            @PathVariable Long courseId,
            @PageableDefault(size = 20) Pageable pageable) {
        return courseService.getCourseEnrollments(creatorId, courseId, pageable);
    }
}
