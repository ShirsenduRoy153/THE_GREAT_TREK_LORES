package com.thegreattreklores.user.entity;

import com.thegreattreklores.common.entity.BaseEntity;
import com.thegreattreklores.user.enums.ExperienceLevel;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserProfile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "profile_photo_url", length = 500)
    private String profilePhotoUrl;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String occupation;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Enumerated(EnumType.STRING)
    @Column(name = "trekking_experience_level", length = 30)
    @Builder.Default
    private ExperienceLevel trekkingExperienceLevel = ExperienceLevel.BEGINNER;
}
