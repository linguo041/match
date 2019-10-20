package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEDaXiaoPk is a Querydsl query type for EDaXiaoPk
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEDaXiaoPk extends EntityPathBase<EDaXiaoPk> {

    private static final long serialVersionUID = -1832962424L;

    public static final QEDaXiaoPk eDaXiaoPk = new QEDaXiaoPk("eDaXiaoPk");

    public final NumberPath<Float> currentAWin = createNumber("currentAWin", Float.class);

    public final NumberPath<Float> currentHWin = createNumber("currentHWin", Float.class);

    public final NumberPath<Float> currentPk = createNumber("currentPk", Float.class);

    public final NumberPath<Float> daChangeRate = createNumber("daChangeRate", Float.class);

    public final NumberPath<Float> hours = createNumber("hours", Float.class);

    public final NumberPath<Float> mainAWin = createNumber("mainAWin", Float.class);

    public final NumberPath<Float> mainHWin = createNumber("mainHWin", Float.class);

    public final NumberPath<Float> mainPk = createNumber("mainPk", Float.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> originAWin = createNumber("originAWin", Float.class);

    public final NumberPath<Float> originHWin = createNumber("originHWin", Float.class);

    public final NumberPath<Float> originPk = createNumber("originPk", Float.class);

    public final NumberPath<Float> xiaoChangeRate = createNumber("xiaoChangeRate", Float.class);

    public QEDaXiaoPk(String variable) {
        super(EDaXiaoPk.class, forVariable(variable));
    }

    public QEDaXiaoPk(Path<? extends EDaXiaoPk> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEDaXiaoPk(PathMetadata<?> metadata) {
        super(EDaXiaoPk.class, metadata);
    }

}

