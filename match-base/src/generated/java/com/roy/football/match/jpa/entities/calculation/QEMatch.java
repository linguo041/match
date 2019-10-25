package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEMatch is a Querydsl query type for EMatch
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEMatch extends EntityPathBase<EMatch> {

    private static final long serialVersionUID = -1195542284L;

    public static final QEMatch eMatch = new QEMatch("eMatch");

    public final NumberPath<Long> fmMatchId = createNumber("fmMatchId", Long.class);

    public final NumberPath<Long> guestId = createNumber("guestId", Long.class);

    public final StringPath guestName = createString("guestName");

    public final NumberPath<Long> hostId = createNumber("hostId", Long.class);

    public final StringPath hostName = createString("hostName");

    public final EnumPath<com.roy.football.match.base.League> league = createEnum("league", com.roy.football.match.base.League.class);

    public final NumberPath<Long> matchDayId = createNumber("matchDayId", Long.class);

    public final DateTimePath<java.util.Date> matchTime = createDateTime("matchTime", java.util.Date.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Long> okoooMatchId = createNumber("okoooMatchId", Long.class);

    public final EnumPath<com.roy.football.match.OFN.CalculationType> phase = createEnum("phase", com.roy.football.match.OFN.CalculationType.class);

    public QEMatch(String variable) {
        super(EMatch.class, forVariable(variable));
    }

    public QEMatch(Path<? extends EMatch> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEMatch(PathMetadata<?> metadata) {
        super(EMatch.class, metadata);
    }

}

