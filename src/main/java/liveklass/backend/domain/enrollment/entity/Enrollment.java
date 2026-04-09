package liveklass.backend.domain.enrollment.entity;

import jakarta.persistence.*;
import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "enrollments",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Enrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    private LocalDateTime confirmedAt;

    private LocalDateTime cancelledAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Enrollment(Course course, User user) {
        this.course = course;
        this.user = user;
        this.status = EnrollmentStatus.PENDING;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void confirm() {
        if (this.status != EnrollmentStatus.PENDING) {
            throw new IllegalStateException("Only PENDING enrollment can be confirmed");
        }
        this.status = EnrollmentStatus.CONFIRMED;
        this.confirmedAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void cancel() {
        if (this.status == EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("Already cancelled");
        }
        if (this.status == EnrollmentStatus.CONFIRMED) {
            validateCancellationPeriod();
        }
        this.status = EnrollmentStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void reEnroll() {
        if (this.status != EnrollmentStatus.CANCELLED) {
            throw new IllegalStateException("Only CANCELLED enrollment can be re-enrolled");
        }
        this.status = EnrollmentStatus.PENDING;
        this.cancelledAt = null;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isCancelled() {
        return this.status == EnrollmentStatus.CANCELLED;
    }

    private void validateCancellationPeriod() {
        if (this.confirmedAt == null || this.confirmedAt.plusDays(7).isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("Cancellation period has expired (7 days after confirmation)");
        }
    }
}
