package liveklass.backend.domain.course.entity;

import jakarta.persistence.*;
import liveklass.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "courses")
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Course {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id", nullable = false)
    private User creator;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private int price;

    @Column(nullable = false)
    private int capacity;

    @Column(nullable = false)
    private int enrolledCount;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CourseStatus status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public Course(User creator, String title, String description, int price, int capacity,
                  LocalDate startDate, LocalDate endDate) {
        this.creator = creator;
        this.title = title;
        this.description = description;
        this.price = price;
        this.capacity = capacity;
        this.enrolledCount = 0;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = CourseStatus.DRAFT;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(CourseStatus newStatus) {
        validateStatusTransition(newStatus);
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }

    public boolean isOpen() {
        return this.status == CourseStatus.OPEN;
    }

    public boolean isFull() {
        return this.enrolledCount >= this.capacity;
    }

    public void increaseEnrolledCount() {
        if (isFull()) {
            throw new IllegalStateException("Course is already full");
        }
        this.enrolledCount++;
        this.updatedAt = LocalDateTime.now();
    }

    public void decreaseEnrolledCount() {
        if (this.enrolledCount <= 0) {
            throw new IllegalStateException("Enrolled count cannot be negative");
        }
        this.enrolledCount--;
        this.updatedAt = LocalDateTime.now();
    }

    private void validateStatusTransition(CourseStatus newStatus) {
        switch (this.status) {
            case DRAFT -> {
                if (newStatus != CourseStatus.OPEN) {
                    throw new IllegalStateException("DRAFT can only transition to OPEN");
                }
            }
            case OPEN -> {
                if (newStatus != CourseStatus.CLOSED) {
                    throw new IllegalStateException("OPEN can only transition to CLOSED");
                }
            }
            case CLOSED -> throw new IllegalStateException("CLOSED course cannot change status");
        }
    }
}
