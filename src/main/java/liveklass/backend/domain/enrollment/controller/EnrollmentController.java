package liveklass.backend.domain.enrollment.controller;

import liveklass.backend.domain.enrollment.dto.EnrollmentResponse;
import liveklass.backend.domain.enrollment.service.EnrollmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping("/courses/{courseId}/enrollments")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return enrollmentService.enroll(userId, courseId);
    }

    @PatchMapping("/courses/{courseId}/enrollments/confirm")
    public EnrollmentResponse confirm(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return enrollmentService.confirm(userId, courseId);
    }

    @PatchMapping("/courses/{courseId}/enrollments/cancel")
    public EnrollmentResponse cancel(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return enrollmentService.cancel(userId, courseId);
    }

    @GetMapping("/enrollments/me")
    public Page<EnrollmentResponse> getMyEnrollments(
            @RequestHeader("X-User-Id") Long userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return enrollmentService.getMyEnrollments(userId, pageable);
    }
}
