package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QEEuroPlCompany is a Querydsl query type for EEuroPlCompany
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEEuroPlCompany extends EntityPathBase<EEuroPlCompany> {

    private static final long serialVersionUID = 149175971L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEEuroPlCompany eEuroPlCompany = new QEEuroPlCompany("eEuroPlCompany");

    public final EnumPath<com.roy.football.match.OFN.response.Company> company = createEnum("company", com.roy.football.match.OFN.response.Company.class);

    public final NumberPath<Float> currentEDraw = createNumber("currentEDraw", Float.class);

    public final NumberPath<Float> currentELose = createNumber("currentELose", Float.class);

    public final NumberPath<Float> currentEWin = createNumber("currentEWin", Float.class);

    public final NumberPath<Float> drawChange = createNumber("drawChange", Float.class);

    public final QEEuroPlState eEuroPlState;

    public final NumberPath<Float> loseChange = createNumber("loseChange", Float.class);

    public final NumberPath<Float> mainEDraw = createNumber("mainEDraw", Float.class);

    public final NumberPath<Float> mainELose = createNumber("mainELose", Float.class);

    public final NumberPath<Float> mainEWin = createNumber("mainEWin", Float.class);

    public final NumberPath<Float> originEDraw = createNumber("originEDraw", Float.class);

    public final NumberPath<Float> originELose = createNumber("originELose", Float.class);

    public final NumberPath<Float> originEWin = createNumber("originEWin", Float.class);

    public final NumberPath<Float> smDrawDiff = createNumber("smDrawDiff", Float.class);

    public final NumberPath<Float> smLoseDiff = createNumber("smLoseDiff", Float.class);

    public final NumberPath<Float> smWinDiff = createNumber("smWinDiff", Float.class);

    public final NumberPath<Float> winChange = createNumber("winChange", Float.class);

    public QEEuroPlCompany(String variable) {
        this(EEuroPlCompany.class, forVariable(variable), INITS);
    }

    public QEEuroPlCompany(Path<? extends EEuroPlCompany> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QEEuroPlCompany(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QEEuroPlCompany(PathMetadata<?> metadata, PathInits inits) {
        this(EEuroPlCompany.class, metadata, inits);
    }

    public QEEuroPlCompany(Class<? extends EEuroPlCompany> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.eEuroPlState = inits.isInitialized("eEuroPlState") ? new QEEuroPlState(forProperty("eEuroPlState")) : null;
    }

}

