package liveklass.backend.domain.waitlist.service;

import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.course.repository.CourseRepository;
import liveklass.backend.domain.enrollment.repository.EnrollmentRepository;
import liveklass.backend.domain.user.entity.User;
import liveklass.backend.domain.user.repository.UserRepository;
import liveklass.backend.domain.waitlist.dto.WaitlistResponse;
import liveklass.backend.domain.waitlist.entity.Waitlist;
import liveklass.backend.domain.waitlist.repository.WaitlistRepository;
import liveklass.backend.global.exception.BadRequestException;
import liveklass.backend.global.exception.ConflictException;
import liveklass.backend.global.exception.ForbiddenException;
import liveklass.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WaitlistService {

    private final WaitlistRepository waitlistRepository;
    private final CourseRepository courseRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;

    @Transactional
    public WaitlistResponse register(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (!user.isStudent()) {
            throw new ForbiddenException("Only STUDENT can register to waitlist");
        }

        Course course = courseRepository.findByIdWithLock(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        if (!course.isOpen()) {
            throw new BadRequestException("Course is not open");
        }

        if (!course.isFull()) {
            throw new BadRequestException("Course still has available seats. Please enroll directly");
        }

        enrollmentRepository.findByCourse_IdAndUser_Id(courseId, userId)
                .filter(e -> !e.isCancelled())
                .ifPresent(e -> { throw new ConflictException("Already enrolled in this course"); });

        if (waitlistRepository.existsByCourse_IdAndUser_Id(courseId, userId)) {
            throw new ConflictException("Already in waitlist");
        }

        int nextOrder = waitlistRepository.findMaxOrderByCourseId(courseId) + 1;

        Waitlist waitlist = Waitlist.builder()
                .course(course)
                .user(user)
                .waitOrder(nextOrder)
                .build();

        Waitlist saved = waitlistRepository.save(waitlist);
        long totalWaiting = waitlistRepository.countByCourse_Id(courseId);

        return WaitlistResponse.from(saved, totalWaiting);
    }

    @Transactional
    public void cancel(Long userId, Long courseId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found: " + userId));

        if (!user.isStudent()) {
            throw new ForbiddenException("Only STUDENT can cancel waitlist");
        }

        Waitlist waitlist = waitlistRepository.findByCourse_IdAndUser_Id(courseId, userId)
                .orElseThrow(() -> new NotFoundException("Not in waitlist"));

        if (!waitlist.getUser().getId().equals(userId)) {
            throw new NotFoundException("Not authorized");
        }

        waitlistRepository.delete(waitlist);
    }

    @Transactional(readOnly = true)
    public WaitlistResponse getMyWaitlist(Long userId, Long courseId) {
        Waitlist waitlist = waitlistRepository.findByCourse_IdAndUser_Id(courseId, userId)
                .orElseThrow(() -> new NotFoundException("Not in waitlist"));

        long totalWaiting = waitlistRepository.countByCourse_Id(courseId);
        return WaitlistResponse.from(waitlist, totalWaiting);
    }
}
