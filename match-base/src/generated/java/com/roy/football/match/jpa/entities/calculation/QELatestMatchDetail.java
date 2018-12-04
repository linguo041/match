package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QELatestMatchDetail is a Querydsl query type for ELatestMatchDetail
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QELatestMatchDetail extends EntityPathBase<ELatestMatchDetail> {

    private static final long serialVersionUID = 526972190L;

    public static final QELatestMatchDetail eLatestMatchDetail = new QELatestMatchDetail("eLatestMatchDetail");

    public final NumberPath<Float> gVariation = createNumber("gVariation", Float.class);

    public final NumberPath<Float> matchGoal = createNumber("matchGoal", Float.class);

    public final NumberPath<Float> matchMiss = createNumber("matchMiss", Float.class);

    public final NumberPath<Float> mVariation = createNumber("mVariation", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> point = createNumber("point", Float.class);

    public final EnumPath<com.roy.football.match.base.LatestMatchMatrixType> type = createEnum("type", com.roy.football.match.base.LatestMatchMatrixType.class);

    public final NumberPath<Float> winDrawPkRate = createNumber("winDrawPkRate", Float.class);

    public final NumberPath<Float> winDrawRate = createNumber("winDrawRate", Float.class);

    public final NumberPath<Float> winPkRate = createNumber("winPkRate", Float.class);

    public final NumberPath<Float> winRate = createNumber("winRate", Float.class);

    public QELatestMatchDetail(String variable) {
        super(ELatestMatchDetail.class, forVariable(variable));
    }

    public QELatestMatchDetail(Path<? extends ELatestMatchDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QELatestMatchDetail(PathMetadata<?> metadata) {
        super(ELatestMatchDetail.class, metadata);
    }

}

