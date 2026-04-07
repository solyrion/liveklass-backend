package liveklass.backend.domain.waitlist.entity;

import jakarta.persistence.*;
import liveklass.backend.domain.course.entity.Course;
import liveklass.backend.domain.user.entity.User;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "waitlists",
    uniqueConstraints = @UniqueConstraint(columnNames = {"course_id", "user_id"})
)
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Waitlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "wait_order", nullable = false)
    private int waitOrder;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Builder
    public Waitlist(Course course, User user, int waitOrder) {
        this.course = course;
        this.user = user;
        this.waitOrder = waitOrder;
        this.createdAt = LocalDateTime.now();
    }
}
