package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QEEuroPlState is a Querydsl query type for EEuroPlState
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEEuroPlState extends EntityPathBase<EEuroPlState> {

    private static final long serialVersionUID = -1169287113L;

    public static final QEEuroPlState eEuroPlState = new QEEuroPlState("eEuroPlState");

    public final NumberPath<Float> avgDraw = createNumber("avgDraw", Float.class);

    public final NumberPath<Float> avgLose = createNumber("avgLose", Float.class);

    public final NumberPath<Float> avgWin = createNumber("avgWin", Float.class);

    public final SetPath<EEuroPlCompany, QEEuroPlCompany> companyPls = this.<EEuroPlCompany, QEEuroPlCompany>createSet("companyPls", EEuroPlCompany.class, QEEuroPlCompany.class, PathInits.DIRECT2);

    public final NumberPath<Float> mainAvgDrawDiff = createNumber("mainAvgDrawDiff", Float.class);

    public final NumberPath<Float> mainAvgLoseDiff = createNumber("mainAvgLoseDiff", Float.class);

    public final NumberPath<Float> mainAvgWinDiff = createNumber("mainAvgWinDiff", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public QEEuroPlState(String variable) {
        super(EEuroPlState.class, forVariable(variable));
    }

    public QEEuroPlState(Path<? extends EEuroPlState> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEEuroPlState(PathMetadata<?> metadata) {
        super(EEuroPlState.class, metadata);
    }

}

