package app.schoolbully.model.entity;

import app.schoolbully.model.enums.IssueType;
import app.schoolbully.model.enums.RecommendedAction;
import app.schoolbully.model.enums.SignalSource;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "signals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Signal {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String bodyText;

    @Column(nullable = false)
    private boolean anonymous;

    @Column(nullable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    // AI Response - Student-facing message
    @Column(columnDefinition = "TEXT")
    private String studentResponse;

    // Classification fields
    @Enumerated(EnumType.STRING)
    private SignalSource source;

    @Enumerated(EnumType.STRING)
    private IssueType type;

    @Column(nullable = false)
    private Integer urgency = 1; // 1-5

    @Column(nullable = false)
    private Integer severity = 1; // 1-5

    @Column(nullable = false)
    private Integer importance = 1; // 1-5

    @Column(nullable = false)
    private Integer credibility = 1; // 1-5

    @Enumerated(EnumType.STRING)
    private RecommendedAction recommendedAction;

    // Legacy fields (for backward compatibility, can be removed later)
    @Column(columnDefinition = "TEXT")
    private String aiAdvice; // Maps to studentResponse

    @Column(nullable = false)
    private Integer seriousnessScore = 0; // Maps to severity * 20

    // Student information (if not anonymous)
    private String studentName;
    private String studentPhoneNumber;

    @Column(nullable = false)
    private boolean authorityNotified = false;

    private String notifiedAuthority;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id")
    private User student;
}

