package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QEMatchClubState is a Querydsl query type for EMatchClubState
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEMatchClubState extends EntityPathBase<EMatchClubState> {

    private static final long serialVersionUID = -1995625753L;

    public static final QEMatchClubState eMatchClubState = new QEMatchClubState("eMatchClubState");

    public final SetPath<EMatchClubDetail, QEMatchClubDetail> clubStateDetails = this.<EMatchClubDetail, QEMatchClubDetail>createSet("clubStateDetails", EMatchClubDetail.class, QEMatchClubDetail.class, PathInits.DIRECT2);

    public final NumberPath<Float> guestAttHostDefInx = createNumber("guestAttHostDefInx", Float.class);

    public final NumberPath<Long> guestId = createNumber("guestId", Long.class);

    public final StringPath guestLabel = createString("guestLabel");

    public final EnumPath<com.roy.football.match.base.TeamLevel> guestLevel = createEnum("guestLevel", com.roy.football.match.base.TeamLevel.class);

    public final NumberPath<Float> hostAttGuestDefInx = createNumber("hostAttGuestDefInx", Float.class);

    public final NumberPath<Long> hostId = createNumber("hostId", Long.class);

    public final StringPath hostLabel = createString("hostLabel");

    public final EnumPath<com.roy.football.match.base.TeamLevel> hostLevel = createEnum("hostLevel", com.roy.football.match.base.TeamLevel.class);

    public final EnumPath<com.roy.football.match.base.League> league = createEnum("league", com.roy.football.match.base.League.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public QEMatchClubState(String variable) {
        super(EMatchClubState.class, forVariable(variable));
    }

    public QEMatchClubState(Path<? extends EMatchClubState> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEMatchClubState(PathMetadata<?> metadata) {
        super(EMatchClubState.class, metadata);
    }

}

