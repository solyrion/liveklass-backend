package liveklass.backend.domain.course.service;

import liveklass.backend.domain.course.dto.CourseCreateRequest;
import liveklass.backend.domain.course.dto.CourseResponse;
import liveklass.backend.domain.course.dto.CourseStatusRequest;
import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.course.entity.CourseStatus;
import liveklass.backend.domain.course.repository.CourseRepository;
import liveklass.backend.domain.user.entity.User;
import liveklass.backend.domain.user.repository.UserRepository;
import liveklass.backend.global.exception.ForbiddenException;
import liveklass.backend.global.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseRepository courseRepository;
    private final UserRepository userRepository;

    @Transactional
    public CourseResponse createCourse(Long creatorId, CourseCreateRequest request) {
        User creator = userRepository.findById(creatorId)
                .orElseThrow(() -> new NotFoundException("User not found: " + creatorId));

        Course course = Course.builder()
                .creator(creator)
                .title(request.getTitle())
                .description(request.getDescription())
                .price(request.getPrice())
                .capacity(request.getCapacity())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return CourseResponse.from(courseRepository.save(course));
    }

    @Transactional
    public CourseResponse changeStatus(Long creatorId, Long courseId, CourseStatusRequest request) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));

        if (!course.getCreator().getId().equals(creatorId)) {
            throw new ForbiddenException("Only the creator can change course status");
        }

        course.changeStatus(request.getStatus());
        return CourseResponse.from(course);
    }

    @Transactional(readOnly = true)
    public Page<CourseResponse> getCourses(CourseStatus status, Pageable pageable) {
        if (status != null) {
            return courseRepository.findAllByStatus(status, pageable)
                    .map(CourseResponse::from);
        }
        return courseRepository.findAll(pageable)
                .map(CourseResponse::from);
    }

    @Transactional(readOnly = true)
    public CourseResponse getCourse(Long courseId) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new NotFoundException("Course not found: " + courseId));
        return CourseResponse.from(course);
    }
}
