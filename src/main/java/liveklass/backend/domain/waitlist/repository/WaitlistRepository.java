package liveklass.backend.domain.waitlist.repository;

import liveklass.backend.domain.waitlist.entity.Waitlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface WaitlistRepository extends JpaRepository<Waitlist, Long> {

    Optional<Waitlist> findByCourse_IdAndUser_Id(Long courseId, Long userId);

    Optional<Waitlist> findFirstByCourse_IdOrderByWaitOrderAsc(Long courseId);

    boolean existsByCourse_IdAndUser_Id(Long courseId, Long userId);

    long countByCourse_Id(Long courseId);

    @Query("SELECT COALESCE(MAX(w.waitOrder), 0) FROM Waitlist w WHERE w.course.id = :courseId")
    int findMaxOrderByCourseId(@Param("courseId") Long courseId);
}
