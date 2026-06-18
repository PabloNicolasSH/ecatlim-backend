package org.scoutsdecanarias.ecatlim_backend.event.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.entity.EducationStage;
import org.scoutsdecanarias.ecatlim_backend.entity.LessonBlock;
import org.scoutsdecanarias.ecatlim_backend.entity.TimelineItem;
import org.scoutsdecanarias.ecatlim_backend.entity.User;
import org.scoutsdecanarias.ecatlim_backend.event.enums.EventStatus;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@Entity
public class Event {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String shortname;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String contents;

    @Column(nullable = false)
    private LocalDateTime startDate;

    @Column(nullable = false)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private String location;

    private Integer theoreticalHours;
    private Integer practicalHours;
    private Integer onlineHours;

    @Column(nullable = false)
    private String organizer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "director_id")
    private User director;

    @ManyToMany
    @JoinTable(
            name = "event_facilitators",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> facilitators = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "event_staff",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> staff = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "event_enrollments",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> attendees = new HashSet<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "education_stage_id")
    private EducationStage educationStage;

    @ManyToMany
    @JoinTable(
            name = "event_lesson_blocks",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "lesson_block_id")
    )
    private Set<LessonBlock> lessonBlocks = new HashSet<>();

    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startTime ASC")
    private List<TimelineItem> timelineItems = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_configuration_id")
    private EventConfiguration eventConfiguration;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventStatus status;

    public void addTimelineItem(TimelineItem item) {
        timelineItems.add(item);
        item.setEvent(this);
    }

    public void removeTimelineItem(TimelineItem item) {
        timelineItems.remove(item);
        item.setEvent(null);
    }

    public String getEducationStageCode() {
        if (lessonBlocks == null || lessonBlocks.isEmpty()) {
            return null;
        }

        LessonBlock lessonBlock = lessonBlocks.iterator().next();
        String code = lessonBlock.getCode();

        if (code == null || !code.contains("-") || code.length() < 3) {
            return null;
        }

        return code.substring(2, code.indexOf("-"));
    }

    public boolean isClosed() {
        if (eventConfiguration == null) return false;
        LocalDateTime today = LocalDateTime.now();

        return today.isAfter(eventConfiguration.getDateCloseInscription());
    }
}
