package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEMatchClubDetail is a Querydsl query type for EMatchClubDetail
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEMatchClubDetail extends EntityPathBase<EMatchClubDetail> {

    private static final long serialVersionUID = 2117369019L;

    public static final QEMatchClubDetail eMatchClubDetail = new QEMatchClubDetail("eMatchClubDetail");

    public final NumberPath<Float> drawLoseRt = createNumber("drawLoseRt", Float.class);

    public final NumberPath<Integer> goals = createNumber("goals", Integer.class);

    public final NumberPath<Integer> misses = createNumber("misses", Integer.class);

    public final NumberPath<Integer> num = createNumber("num", Integer.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Integer> pm = createNumber("pm", Integer.class);

    public final NumberPath<Integer> point = createNumber("point", Integer.class);

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public final EnumPath<com.roy.football.match.base.MatrixType> type = createEnum("type", com.roy.football.match.base.MatrixType.class);

    public final NumberPath<Float> winDrawRt = createNumber("winDrawRt", Float.class);

    public final NumberPath<Integer> winGoals = createNumber("winGoals", Integer.class);

    public final NumberPath<Integer> winLoseDiff = createNumber("winLoseDiff", Integer.class);

    public final NumberPath<Float> winRt = createNumber("winRt", Float.class);

    public QEMatchClubDetail(String variable) {
        super(EMatchClubDetail.class, forVariable(variable));
    }

    public QEMatchClubDetail(Path<? extends EMatchClubDetail> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEMatchClubDetail(PathMetadata<?> metadata) {
        super(EMatchClubDetail.class, metadata);
    }

}

