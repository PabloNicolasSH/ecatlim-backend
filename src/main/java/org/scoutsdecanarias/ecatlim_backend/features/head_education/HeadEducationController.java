package org.scoutsdecanarias.ecatlim_backend.features.head_education;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationEventDto;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationMemberDto;
import org.scoutsdecanarias.ecatlim_backend.features.head_education.dto.HeadEducationMemberHistoryDto;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/head-education")
@PreAuthorize("hasAuthority('HEAD_OF_EDUCATION')")
public class HeadEducationController {

    private final HeadEducationService headEducationService;

    @GetMapping("/members")
    public List<HeadEducationMemberDto> getMembers() {
        return headEducationService.getMembers(currentEmail());
    }

    @GetMapping("/members/{id}/history")
    public HeadEducationMemberHistoryDto getMemberHistory(@PathVariable Integer id) {
        return headEducationService.getMemberHistory(currentEmail(), id);
    }

    @PutMapping("/members/{id}/activate")
    public HeadEducationMemberDto activateMember(@PathVariable Integer id) {
        log.info("METHOD activateMember() - {} activates member {}", currentEmail(), id);
        return headEducationService.setMemberEnabled(currentEmail(), id, true);
    }

    @PutMapping("/members/{id}/deactivate")
    public HeadEducationMemberDto deactivateMember(@PathVariable Integer id) {
        log.info("METHOD deactivateMember() - {} deactivates member {}", currentEmail(), id);
        return headEducationService.setMemberEnabled(currentEmail(), id, false);
    }

    @GetMapping("/events")
    public List<HeadEducationEventDto> getEvents() {
        return headEducationService.getEvents(currentEmail());
    }

    private String currentEmail() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }
}
