package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QELatestMatchState is a Querydsl query type for ELatestMatchState
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QELatestMatchState extends EntityPathBase<ELatestMatchState> {

    private static final long serialVersionUID = -1492739548L;

    public static final QELatestMatchState eLatestMatchState = new QELatestMatchState("eLatestMatchState");

    public final NumberPath<Float> guestAttackToHost = createNumber("guestAttackToHost", Float.class);

    public final NumberPath<Float> guestAttackVariationToHost = createNumber("guestAttackVariationToHost", Float.class);

    public final NumberPath<Float> hostAttackToGuest = createNumber("hostAttackToGuest", Float.class);

    public final NumberPath<Float> hostAttackVariationToGuest = createNumber("hostAttackVariationToGuest", Float.class);

    public final NumberPath<Float> hotPoint = createNumber("hotPoint", Float.class);

    public final SetPath<ELatestMatchDetail, QELatestMatchDetail> latestDetails = this.<ELatestMatchDetail, QELatestMatchDetail>createSet("latestDetails", ELatestMatchDetail.class, QELatestMatchDetail.class, PathInits.DIRECT2);

    public final EnumPath<com.roy.football.match.base.League> league = createEnum("league", com.roy.football.match.base.League.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> pkByLatestMatches = createNumber("pkByLatestMatches", Float.class);

    public QELatestMatchState(String variable) {
        super(ELatestMatchState.class, forVariable(variable));
    }

    public QELatestMatchState(Path<? extends ELatestMatchState> path) {
        super(path.getType(), path.getMetadata());
    }

    public QELatestMatchState(PathMetadata<?> metadata) {
        super(ELatestMatchState.class, metadata);
    }

}

