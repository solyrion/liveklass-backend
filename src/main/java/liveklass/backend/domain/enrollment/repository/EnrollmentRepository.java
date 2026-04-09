package liveklass.backend.domain.enrollment.repository;

import liveklass.backend.domain.enrollment.entity.Enrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

    Optional<Enrollment> findByCourse_IdAndUser_Id(Long courseId, Long userId);

    Page<Enrollment> findAllByUser_Id(Long userId, Pageable pageable);

    Page<Enrollment> findAllByCourse_Id(Long courseId, Pageable pageable);
}
