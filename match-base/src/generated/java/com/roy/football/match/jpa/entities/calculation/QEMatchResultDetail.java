package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEMatchResultDetail is a Querydsl query type for EMatchResultDetail
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEMatchResultDetail extends EntityPathBase<EMatchResultDetail> {

    private static final long serialVersionUID = 1068663266L;

    public static final QEMatchResultDetail eMatchResultDetail = new QEMatchResultDetail("eMatchResultDetail");

    public final NumberPath<Integer> guestCorner = createNumber("guestCorner", Integer.class);

    public final NumberPath<Integer> guestFault = createNumber("guestFault", Integer.class);

    public final NumberPath<Long> guestId = createNumber("guestId", Long.class);

    public final StringPath guestName = createString("guestName");

    public final NumberPath<Integer> guestOffside = createNumber("guestOffside", Integer.class);

    public final NumberPath<Integer> guestSave = createNumber("guestSave", Integer.class);

    public final NumberPath<Integer> guestScore = createNumber("guestScore", Integer.class);

    public final NumberPath<Integer> guestShot = createNumber("guestShot", Integer.class);

    public final NumberPath<Integer> guestShotOnTarget = createNumber("guestShotOnTarget", Integer.class);

    public final NumberPath<Float> guestTime = createNumber("guestTime", Float.class);

    public final NumberPath<Integer> guestYellowCard = createNumber("guestYellowCard", Integer.class);

    public final NumberPath<Integer> hostCorner = createNumber("hostCorner", Integer.class);

    public final NumberPath<Integer> hostFault = createNumber("hostFault", Integer.class);

    public final NumberPath<Long> hostId = createNumber("hostId", Long.class);

    public final StringPath hostName = createString("hostName");

    public final NumberPath<Integer> hostOffside = createNumber("hostOffside", Integer.class);

    public final NumberPath<Integer> hostSave = createNumber("hostSave", Integer.class);

    public final NumberPath<Integer> hostScore = createNumber("hostScore", Integer.class);

    public final NumberPath<Integer> hostShot = createNumber("hostShot", Integer.class);

    public final NumberPath<Integer> hostShotOnTarget = createNumber("hostShotOnTarget", Integer.class);

    public final NumberPath<Float> hostTime = createNumber("hostTime", Float.class);

    public final NumberPath<Integer> hostYellowCard = createNumber("hostYellowCard", Integer.class);

    public final EnumPath<com.roy.football.match.base.League> league = createEnum("league", com.roy.football.match.base.League.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public QEMatchResultDetail(String variable) {
        super(EMatchResultDetail.class, forVariable(variable));
    }

    public QEMatchResultDetail(Path<? extends EMatchResultDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEMatchResultDetail(PathMetadata<?> metadata) {
        super(EMatchResultDetail.class, metadata);
    }

}

