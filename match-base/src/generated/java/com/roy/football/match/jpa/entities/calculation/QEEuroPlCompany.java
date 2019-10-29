package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEEuroPlCompany is a Querydsl query type for EEuroPlCompany
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEEuroPlCompany extends EntityPathBase<EEuroPlCompany> {

    private static final long serialVersionUID = 149175971L;

    public static final QEEuroPlCompany eEuroPlCompany = new QEEuroPlCompany("eEuroPlCompany");

    public final EnumPath<com.roy.football.match.OFN.response.Company> company = createEnum("company", com.roy.football.match.OFN.response.Company.class);

    public final NumberPath<Float> currentEDraw = createNumber("currentEDraw", Float.class);

    public final NumberPath<Float> currentELose = createNumber("currentELose", Float.class);

    public final NumberPath<Float> currentEWin = createNumber("currentEWin", Float.class);

    public final NumberPath<Float> drawChange = createNumber("drawChange", Float.class);

    public final NumberPath<Float> loseChange = createNumber("loseChange", Float.class);

    public final NumberPath<Float> mainEDraw = createNumber("mainEDraw", Float.class);

    public final NumberPath<Float> mainELose = createNumber("mainELose", Float.class);

    public final NumberPath<Float> mainEWin = createNumber("mainEWin", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> originEDraw = createNumber("originEDraw", Float.class);

    public final NumberPath<Float> originELose = createNumber("originELose", Float.class);

    public final NumberPath<Float> originEWin = createNumber("originEWin", Float.class);

    public final NumberPath<Float> smDrawDiff = createNumber("smDrawDiff", Float.class);

    public final NumberPath<Float> smLoseDiff = createNumber("smLoseDiff", Float.class);

    public final NumberPath<Float> smWinDiff = createNumber("smWinDiff", Float.class);

    public final NumberPath<Float> winChange = createNumber("winChange", Float.class);

    public QEEuroPlCompany(String variable) {
        super(EEuroPlCompany.class, forVariable(variable));
    }

    public QEEuroPlCompany(Path<? extends EEuroPlCompany> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEEuroPlCompany(PathMetadata<?> metadata) {
        super(EEuroPlCompany.class, metadata);
    }

}

