package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEAsiaPk is a Querydsl query type for EAsiaPk
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEAsiaPk extends EntityPathBase<EAsiaPk> {

    private static final long serialVersionUID = 1265638166L;

    public static final QEAsiaPk eAsiaPk = new QEAsiaPk("eAsiaPk");

    public final NumberPath<Float> awinChangeRate = createNumber("awinChangeRate", Float.class);

    public final EnumPath<com.roy.football.match.OFN.response.Company> company = createEnum("company", com.roy.football.match.OFN.response.Company.class);

    public final NumberPath<Float> currentAWin = createNumber("currentAWin", Float.class);

    public final NumberPath<Float> currentHWin = createNumber("currentHWin", Float.class);

    public final NumberPath<Float> currentPk = createNumber("currentPk", Float.class);

    public final NumberPath<Float> hours = createNumber("hours", Float.class);

    public final NumberPath<Float> hwinChangeRate = createNumber("hwinChangeRate", Float.class);

    public final NumberPath<Float> mainAWin = createNumber("mainAWin", Float.class);

    public final NumberPath<Float> mainHWin = createNumber("mainHWin", Float.class);

    public final NumberPath<Float> mainPk = createNumber("mainPk", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> originAWin = createNumber("originAWin", Float.class);

    public final NumberPath<Float> originHWin = createNumber("originHWin", Float.class);

    public final NumberPath<Float> originPk = createNumber("originPk", Float.class);

    public QEAsiaPk(String variable) {
        super(EAsiaPk.class, forVariable(variable));
    }

    public QEAsiaPk(Path<? extends EAsiaPk> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEAsiaPk(PathMetadata<?> metadata) {
        super(EAsiaPk.class, metadata);
    }

}

