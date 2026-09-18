package org.scoutsdecanarias.ecatlim_backend.features.activity.entity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrimaryKeyJoinColumn;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@PrimaryKeyJoinColumn(name = "activity_id")
public class ForumActivity extends Activity {
    @OneToMany(mappedBy = "activity", cascade = CascadeType.ALL)
    private List<ForumPublication> publications;
}
