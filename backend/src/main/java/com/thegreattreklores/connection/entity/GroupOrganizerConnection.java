package com.thegreattreklores.connection.entity;

import com.thegreattreklores.common.entity.BaseEntity;
import com.thegreattreklores.connection.enums.ConnectionInitiator;
import com.thegreattreklores.connection.enums.ConnectionStatus;
import com.thegreattreklores.group.entity.TripGroup;
import com.thegreattreklores.organizer.entity.Organizer;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@Entity
@Table(
    name = "group_organizer_connections",
    uniqueConstraints = @UniqueConstraint(columnNames = {"group_id", "organizer_id"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GroupOrganizerConnection extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id", nullable = false)
    private TripGroup group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organizer_id", nullable = false)
    private Organizer organizer;

    @Enumerated(EnumType.STRING)
    @Column(name = "initiated_by", nullable = false, length = 30)
    private ConnectionInitiator initiatedBy;

    @Column(columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private ConnectionStatus status = ConnectionStatus.PENDING;
}
