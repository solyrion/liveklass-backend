package liveklass.backend.domain.waitlist.dto;

import liveklass.backend.domain.waitlist.entity.Waitlist;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class WaitlistResponse {

    private Long id;
    private Long courseId;
    private Long userId;
    private int waitOrder;
    private long totalWaiting;
    private LocalDateTime createdAt;

    public static WaitlistResponse from(Waitlist waitlist, long totalWaiting) {
        return WaitlistResponse.builder()
                .id(waitlist.getId())
                .courseId(waitlist.getCourse().getId())
                .userId(waitlist.getUser().getId())
                .waitOrder(waitlist.getWaitOrder())
                .totalWaiting(totalWaiting)
                .createdAt(waitlist.getCreatedAt())
                .build();
    }
}
