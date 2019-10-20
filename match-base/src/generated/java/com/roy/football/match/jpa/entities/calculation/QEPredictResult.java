package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEPredictResult is a Querydsl query type for EPredictResult
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEPredictResult extends EntityPathBase<EPredictResult> {

    private static final long serialVersionUID = -459955579L;

    public static final QEPredictResult ePredictResult = new QEPredictResult("ePredictResult");

    public final NumberPath<Float> guestScore = createNumber("guestScore", Float.class);

    public final NumberPath<Float> hostScore = createNumber("hostScore", Float.class);

    public final NumberPath<Float> lastMatchPk = createNumber("lastMatchPk", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> predictPk = createNumber("predictPk", Float.class);

    public QEPredictResult(String variable) {
        super(EPredictResult.class, forVariable(variable));
    }

    public QEPredictResult(Path<? extends EPredictResult> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEPredictResult(PathMetadata<?> metadata) {
        super(EPredictResult.class, metadata);
    }

}

