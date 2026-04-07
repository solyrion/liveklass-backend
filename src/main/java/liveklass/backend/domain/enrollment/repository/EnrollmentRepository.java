package liveklass.backend.domain.enrollment.repository;

import liveklass.backend.domain.enrollment.entity.Enrollment;
import liveklass.backend.domain.enrollment.entity.EnrollmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByCourse_IdAndUser_Id(Long courseId, Long userId);

    boolean existsByCourse_IdAndUser_IdAndStatusNot(Long courseId, Long userId, EnrollmentStatus status);

    Page<Enrollment> findAllByUser_Id(Long userId, Pageable pageable);
}
