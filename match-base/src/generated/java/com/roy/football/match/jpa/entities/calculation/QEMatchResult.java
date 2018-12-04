package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;
import com.mysema.query.types.path.PathInits;


/**
 * QEMatchResult is a Querydsl query type for EMatchResult
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEMatchResult extends EntityPathBase<EMatchResult> {

    private static final long serialVersionUID = 1608951345L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QEMatchResult eMatchResult = new QEMatchResult("eMatchResult");

    public final EnumPath<com.roy.football.match.base.ResultGroup> dxResult = createEnum("dxResult", com.roy.football.match.base.ResultGroup.class);

    public final QEMatchResultDetail eMatchResultDetail;

    public final NumberPath<Integer> guestScore = createNumber("guestScore", Integer.class);

    public final NumberPath<Integer> hostScore = createNumber("hostScore", Integer.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final EnumPath<com.roy.football.match.base.ResultGroup> pkResult = createEnum("pkResult", com.roy.football.match.base.ResultGroup.class);

    public final EnumPath<com.roy.football.match.base.ResultGroup> plResult = createEnum("plResult", com.roy.football.match.base.ResultGroup.class);

    public QEMatchResult(String variable) {
        this(EMatchResult.class, forVariable(variable), INITS);
    }

    public QEMatchResult(Path<? extends EMatchResult> path) {
        this(path.getType(), path.getMetadata(), path.getMetadata().isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QEMatchResult(PathMetadata<?> metadata) {
        this(metadata, metadata.isRoot() ? INITS : PathInits.DEFAULT);
    }

    public QEMatchResult(PathMetadata<?> metadata, PathInits inits) {
        this(EMatchResult.class, metadata, inits);
    }

    public QEMatchResult(Class<? extends EMatchResult> type, PathMetadata<?> metadata, PathInits inits) {
        super(type, metadata, inits);
        this.eMatchResultDetail = inits.isInitialized("eMatchResultDetail") ? new QEMatchResultDetail(forProperty("eMatchResultDetail")) : null;
    }

}

