package liveklass.backend.domain.enrollment.service;

import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.course.repository.CourseRepository;
import liveklass.backend.domain.enrollment.dto.EnrollmentResponse;
import liveklass.backend.domain.enrollment.entity.Enrollment;
import liveklass.backend.domain.enrollment.entity.EnrollmentStatus;
import liveklass.backend.domain.enrollment.repository.EnrollmentRepository;
import liveklass.backend.domain.user.entity.User;
import liveklass.backend.domain.user.repository.UserRepository;
import liveklass.backend.domain.waitlist.entity.Waitlist;
import liveklass.backend.domain.waitlist.repository.WaitlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final WaitlistRepository waitlistRepository;

    @Transactional
    public EnrollmentResponse enroll(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));

        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Course not found: " + courseId));

        if (!course.isOpen()) {
            throw new IllegalStateException("Course is not open for enrollment");
        }

        if (enrollmentRepository.existsByCourse_IdAndUser_IdAndStatusNot(courseId, userId, EnrollmentStatus.CANCELLED)) {
            throw new IllegalStateException("Already enrolled in this course");
        }

        if (course.isFull()) {
            throw new IllegalStateException("Course is full");
        }

        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .user(user)
                .build();

        course.increaseEnrolledCount();
        return EnrollmentResponse.from(enrollmentRepository.save(enrollment));
    }

    @Transactional
    public EnrollmentResponse confirm(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }

        enrollment.confirm();
        return EnrollmentResponse.from(enrollment);
    }

    @Transactional
    public EnrollmentResponse cancel(Long userId, Long enrollmentId) {
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Enrollment not found: " + enrollmentId));

        if (!enrollment.getUser().getId().equals(userId)) {
            throw new IllegalArgumentException("Not authorized");
        }

        Course course = courseRepository.findByIdWithLock(enrollment.getCourse().getId())
                .orElseThrow(() -> new IllegalArgumentException("Course not found"));

        enrollment.cancel();
        course.decreaseEnrolledCount();

        promoteFromWaitlist(course);

        return EnrollmentResponse.from(enrollment);
    }

    @Transactional(readOnly = true)
    public Page<EnrollmentResponse> getMyEnrollments(Long userId, Pageable pageable) {
        return enrollmentRepository.findAllByUser_Id(userId, pageable)
                .map(EnrollmentResponse::from);
    }

    private void promoteFromWaitlist(Course course) {
        if (!course.isOpen()) {
            return;
        }

        waitlistRepository.findFirstByCourse_IdOrderByWaitOrderAsc(course.getId())
                .ifPresent(waitlist -> {
                    User waitlistUser = waitlist.getUser();
                    Enrollment newEnrollment = Enrollment.builder()
                            .course(course)
                            .user(waitlistUser)
                            .build();
                    enrollmentRepository.save(newEnrollment);
                    course.increaseEnrolledCount();
                    waitlistRepository.delete(waitlist);
                });
    }
}
