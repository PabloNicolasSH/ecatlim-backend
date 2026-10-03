package org.scoutsdecanarias.ecatlim_backend.core.security;

import org.junit.jupiter.api.Test;
import org.scoutsdecanarias.ecatlim_backend.features.admin_dashboard.DashboardController;
import org.scoutsdecanarias.ecatlim_backend.features.admin_dashboard.DashboardService;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.controller.TagController;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.entity.Tag;
import org.scoutsdecanarias.ecatlim_backend.features.learning_resource.service.TagService;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupController;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupDto;
import org.scoutsdecanarias.ecatlim_backend.features.scout_group.ScoutGroupService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringJUnitConfig(AdminEndpointsAuthorizationTest.Config.class)
class AdminEndpointsAuthorizationTest {

    @Configuration
    @EnableMethodSecurity
    @Import({ScoutGroupController.class, TagController.class, DashboardController.class})
    static class Config {
        @Bean ScoutGroupService scoutGroupService() { return mock(ScoutGroupService.class); }
        @Bean TagService tagService() {
            TagService service = mock(TagService.class);
            Tag tag = new Tag();
            tag.setId(1);
            tag.setName("tag");
            when(service.createTag(anyString())).thenReturn(tag);
            return service;
        }
        @Bean DashboardService dashboardService() { return mock(DashboardService.class); }
    }

    @Autowired ScoutGroupController scoutGroupController;
    @Autowired TagController tagController;
    @Autowired DashboardController dashboardController;

    private final ScoutGroupDto group = new ScoutGroupDto(null, "Grupo", 35, 1, "g@test.com");

    @Test
    @WithMockUser(authorities = "STUDENT")
    void studentIsDenied() {
        assertThatThrownBy(() -> scoutGroupController.addScoutGroup(group)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> scoutGroupController.updateScoutGroup(1, group)).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> tagController.createTag("tag")).isInstanceOf(AccessDeniedException.class);
        assertThatThrownBy(() -> dashboardController.getDashboardData()).isInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(authorities = "ADMIN")
    void adminManagesScoutGroupsAndTags() {
        assertThatCode(() -> scoutGroupController.addScoutGroup(group)).doesNotThrowAnyException();
        assertThatCode(() -> tagController.createTag("tag")).doesNotThrowAnyException();
    }

    @Test
    @WithMockUser(authorities = "TRAINER")
    void trainerSeesDashboardAndCreatesTagsButNotScoutGroups() {
        assertThatCode(() -> dashboardController.getDashboardData()).doesNotThrowAnyException();
        assertThatCode(() -> tagController.createTag("tag")).doesNotThrowAnyException();
        assertThatThrownBy(() -> scoutGroupController.addScoutGroup(group)).isInstanceOf(AccessDeniedException.class);
    }
}
