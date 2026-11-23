package com.thegreattreklores.group.entity;

import com.thegreattreklores.common.entity.BaseEntity;
import com.thegreattreklores.group.enums.GroupStatus;
import com.thegreattreklores.group.enums.GroupVisibility;
import com.thegreattreklores.user.entity.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "trip_groups")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripGroup extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdByUser;

    @Column(nullable = false, length = 150)
    private String name;

    @Column(nullable = false, length = 100)
    private String destination;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "preferred_start_date")
    private LocalDate preferredStartDate;

    @Column(name = "preferred_end_date")
    private LocalDate preferredEndDate;

    @Column(name = "dates_flexible", nullable = false)
    @Builder.Default
    private Boolean datesFlexible = true;

    @Column(name = "max_members", nullable = false)
    @Builder.Default
    private Integer maxMembers = 6;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private GroupVisibility visibility = GroupVisibility.PUBLIC;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private GroupStatus status = GroupStatus.OPEN;

    @OneToMany(mappedBy = "group", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GroupMembership> memberships = new ArrayList<>();
}
