package org.scoutsdecanarias.ecatlim_backend.features.user.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.scoutsdecanarias.ecatlim_backend.core.exception.EcatlimException;
import org.scoutsdecanarias.ecatlim_backend.features.user.dto.*;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user.enums.Role;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.StudentOverviewService;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.TrainingTeamService;
import org.scoutsdecanarias.ecatlim_backend.features.user.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@AllArgsConstructor
@RequestMapping("/user")
public class UserController {

    private final UserService userService;
    private final StudentOverviewService studentOverviewService;
    private final TrainingTeamService trainingTeamService;

    @GetMapping("/me")
    public UserDto getUserInfo() {
        log.info("METHOD getUserInfo() - Get user info for principal: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.getUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()));
    }

    @PutMapping("/update/me")
    public UserDto updateMyUserInfo(@RequestBody UserMeFormDto userMeFormDto) {
        log.info("METHOD updateMyUserInfo() - Update user info for principal: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.updateUserMe(userMeFormDto));
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping("/event-staff/{role}")
    public List<SimpleUserDto> getEventStaff(@PathVariable Role role) {
        if (role != Role.EVENT_DIRECTOR && role != Role.TRAINER) {
            throw new EcatlimException("Solo se pueden consultar direcciones de evento o equipo de formación", HttpStatus.BAD_REQUEST);
        }
        return SimpleUserDto.fromCollection(userService.getUsersByRole(role).stream().filter(User::isEnabled).toList());
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping("/students/overview")
    public StudentOverviewDto getStudentOverview() {
        return studentOverviewService.getOverview();
    }

    @PreAuthorize("hasAnyAuthority('ADMIN', 'MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @GetMapping("/team")
    public List<TeamMemberDto> getTrainingTeam() {
        return trainingTeamService.getTeam();
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT')")
    @PostMapping("/team/add")
    public TeamMemberDto addTeamMember(@RequestBody UserFormDto user) {
        log.info("METHOD addTeamMember - Adding team member: {}, by: {}", user.email(), SecurityContextHolder.getContext().getAuthentication().getName());
        return trainingTeamService.addMember(user);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT')")
    @GetMapping("/team/candidates")
    public List<TeamMemberDto> searchTeamCandidates(@RequestParam("q") String query) {
        return trainingTeamService.searchCandidates(query);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT')")
    @PostMapping("/team/{id}/roles/{role}")
    public TeamMemberDto addTeamRole(@PathVariable Integer id, @PathVariable Role role) {
        log.info("METHOD addTeamRole - Adding role {} to user {} by: {}", role, id, SecurityContextHolder.getContext().getAuthentication().getName());
        return trainingTeamService.addRole(id, role);
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT')")
    @DeleteMapping("/team/{id}/roles/{role}")
    public TeamMemberDto removeTeamRole(@PathVariable Integer id, @PathVariable Role role) {
        log.info("METHOD removeTeamRole - Removing role {} from user {} by: {}", role, id, SecurityContextHolder.getContext().getAuthentication().getName());
        return trainingTeamService.removeRole(id, role);
    }

    @GetMapping("/simpleUsersInfo")
    public List<SimpleUserDto> getSimpleUserInfo() {
        return SimpleUserDto.fromCollection(userService.getActiveUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/allActives")
    public List<UserDto> getActiveUsers() {
        log.info("METHOD getActiveUsers() - Get active users by: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromCollection(userService.getActiveUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/all/{role}")
    public List<UserDto> getUsersByRole(@PathVariable String role) {
        log.info("METHOD getUsersByRole() - Get users by role: {}", role);
        Role r = Role.valueOf(role);
        return UserDto.fromCollection(userService.getUsersByRole(r));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @GetMapping("/admin/allInactives")
    public List<UserDto> getInactiveUsers() {
        log.info("METHOD getInactiveUsers() - Get inactive users by: {}", SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromCollection(userService.getInactiveUsers());
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PostMapping("/admin/add")
    public UserDto addUser(@RequestBody UserFormDto user) {
        log.info("METHOD addUser - Adding user: {}, by: {}", user, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.addUser(user));
    }

    @PreAuthorize("hasAnyAuthority('MANAGER_DIRECTOR', 'MANAGEMENT', 'EVENT_DIRECTOR', 'TRAINER')")
    @PostMapping("/student/add")
    public UserDto addStudent(@RequestBody UserFormDto user) {
        log.info("METHOD addStudent - Adding student: {}, by: {}", user.email(), SecurityContextHolder.getContext().getAuthentication().getName());
        UserFormDto student = new UserFormDto(user.name(), user.surname(), user.email(), user.phone(), user.nif(),
                user.census(), user.address(), user.city(), user.country(), Set.of(Role.STUDENT), user.scoutGroupId());
        return UserDto.fromEntity(userService.addUser(student));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/edit/{id}")
    public UserDto editUser(@PathVariable Integer id, @RequestBody UserFormDto user) {
        log.info("METHOD editUser() - Updating user: {}, by: {}", user, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.updateUser(id, user));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @PutMapping("/admin/activate/{id}")
    public UserDto activateUser(@PathVariable Integer id) {
        log.info("METHOD activateUser - Activating user: {} by: {}", id, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.activateUser(id));
    }

    @PreAuthorize("hasAuthority('ADMIN')")
    @DeleteMapping("/admin/deactivate/{id}")
    public UserDto deactivateUser(@PathVariable Integer id) {
        log.info("METHOD deactivateUser - Deactivating user: {}, by: {}", id, SecurityContextHolder.getContext().getAuthentication().getName());
        return UserDto.fromEntity(userService.deactivateUser(id));
    }

}
