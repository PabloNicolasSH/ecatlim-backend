package org.scoutsdecanarias.ecatlim_backend.features.chat;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.scoutsdecanarias.ecatlim_backend.features.user.entity.User;
import org.scoutsdecanarias.ecatlim_backend.features.user_file.UserFile;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @OneToMany(mappedBy = "chat")
    private List<ChatMessage> chatMessageList;

    @ManyToMany
    @JoinTable(
        name = "chat_members",
        joinColumns = @JoinColumn(name = "chat_id"),
        inverseJoinColumns = @JoinColumn(name = "chat_members_id")
    )
    private List<User> chatMembers;

    private Instant creationDate;

    private String chatName;

    private String chatDescription;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "chat_picture_id", referencedColumnName = "id")
    private UserFile chatPicture;
}
