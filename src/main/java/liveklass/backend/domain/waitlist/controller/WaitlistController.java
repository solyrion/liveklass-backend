package liveklass.backend.domain.waitlist.controller;

import liveklass.backend.domain.waitlist.dto.WaitlistResponse;
import liveklass.backend.domain.waitlist.service.WaitlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/courses/{courseId}/waitlists")
@RequiredArgsConstructor
public class WaitlistController {

    private final WaitlistService waitlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public WaitlistResponse register(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return waitlistService.register(userId, courseId);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        waitlistService.cancel(userId, courseId);
    }

    @GetMapping("/me")
    public WaitlistResponse getMyWaitlist(
            @RequestHeader("X-User-Id") Long userId,
            @PathVariable Long courseId) {
        return waitlistService.getMyWaitlist(userId, courseId);
    }
}
