package liveklass.backend.domain.enrollment;

import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.course.entity.CourseStatus;
import liveklass.backend.domain.course.repository.CourseRepository;
import liveklass.backend.domain.enrollment.repository.EnrollmentRepository;
import liveklass.backend.domain.enrollment.service.EnrollmentService;
import liveklass.backend.domain.user.entity.User;
import liveklass.backend.domain.user.entity.UserRole;
import liveklass.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EnrollmentConcurrencyTest {

    @Autowired
    private EnrollmentService enrollmentService;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EnrollmentRepository enrollmentRepository;

    private Course course;
    private List<User> users;

    @BeforeEach
    void setUp() {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();
        userRepository.deleteAll();

        User creator = userRepository.save(User.builder()
                .name("creator")
                .email("creator@test.com")
                .role(UserRole.CREATOR)
                .build());

        course = courseRepository.save(Course.builder()
                .creator(creator)
                .title("테스트 강의")
                .description("동시성 테스트용")
                .price(10000)
                .capacity(1)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build());

        course.changeStatus(CourseStatus.OPEN);
        courseRepository.save(course);

        users = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            users.add(userRepository.save(User.builder()
                    .name("user" + i)
                    .email("user" + i + "@test.com")
                    .role(UserRole.STUDENT)
                    .build()));
        }
    }

    @Test
    @DisplayName("정원 1명인 강의에 10명이 동시 신청해도 1명만 등록된다")
    void concurrentEnrollment_withCapacity1_onlyOneSucceeds() throws InterruptedException {
        int threadCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = users.get(i).getId();
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    enrollmentService.enroll(userId, course.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    failCount.incrementAndGet();
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        Course result = courseRepository.findById(course.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(1);
        assertThat(result.getEnrolledCount()).isEqualTo(1);
        assertThat(result.getEnrolledCount()).isLessThanOrEqualTo(result.getCapacity());
    }

    @Test
    @DisplayName("정원 30명인 강의에 50명이 동시 신청해도 30명을 초과하지 않는다")
    void concurrentEnrollment_withCapacity30_doesNotExceedCapacity() throws InterruptedException {
        enrollmentRepository.deleteAll();
        courseRepository.deleteAll();

        User creator = userRepository.findByEmail("creator@test.com").orElseThrow();

        int capacity = 30;
        int threadCount = 50;

        Course largeCourse = courseRepository.save(Course.builder()
                .creator(creator)
                .title("대용량 테스트 강의")
                .description("동시성 테스트용")
                .price(10000)
                .capacity(capacity)
                .startDate(LocalDate.now())
                .endDate(LocalDate.now().plusMonths(1))
                .build());

        largeCourse.changeStatus(CourseStatus.OPEN);
        courseRepository.save(largeCourse);

        List<User> manyUsers = new ArrayList<>();
        for (int i = 100; i < 100 + threadCount; i++) {
            manyUsers.add(userRepository.save(User.builder()
                    .name("user" + i)
                    .email("user" + i + "@test.com")
                    .role(UserRole.STUDENT)
                    .build()));
        }

        ExecutorService executorService = Executors.newFixedThreadPool(threadCount);
        CountDownLatch readyLatch = new CountDownLatch(threadCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);

        for (int i = 0; i < threadCount; i++) {
            final Long userId = manyUsers.get(i).getId();
            executorService.submit(() -> {
                readyLatch.countDown();
                try {
                    startLatch.await();
                    enrollmentService.enroll(userId, largeCourse.getId());
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    // 정원 초과 실패는 정상
                }
            });
        }

        readyLatch.await();
        startLatch.countDown();
        executorService.shutdown();
        executorService.awaitTermination(10, java.util.concurrent.TimeUnit.SECONDS);

        Course result = courseRepository.findById(largeCourse.getId()).orElseThrow();

        assertThat(successCount.get()).isEqualTo(capacity);
        assertThat(result.getEnrolledCount()).isEqualTo(capacity);
        assertThat(result.getEnrolledCount()).isLessThanOrEqualTo(result.getCapacity());
    }
}
