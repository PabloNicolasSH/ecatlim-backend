package org.scoutsdecanarias.ecatlim_backend.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import lombok.Getter;
import lombok.Setter;

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
    private String description;

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
            name = "event_enrollments",
            joinColumns = @JoinColumn(name = "event_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> attendees = new HashSet<>();

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

    public void addTimelineItem(TimelineItem item) {
        timelineItems.add(item);
        item.setEvent(this);
    }

    public String getEducationStageCode() {
        LessonBlock lessonBlock = lessonBlocks.iterator().next();
        String code = lessonBlock.getCode();
        return code.substring(2, code.indexOf("-"));
    }
}
