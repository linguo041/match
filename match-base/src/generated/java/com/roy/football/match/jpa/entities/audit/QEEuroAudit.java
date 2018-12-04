package com.roy.football.match.jpa.entities.audit;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEEuroAudit is a Querydsl query type for EEuroAudit
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEEuroAudit extends EntityPathBase<EEuroAudit> {

    private static final long serialVersionUID = -980710517L;

    public static final QEEuroAudit eEuroAudit = new QEEuroAudit("eEuroAudit");

    public final NumberPath<Long> auditId = createNumber("auditId", Long.class);

    public final NumberPath<Float> avgDraw = createNumber("avgDraw", Float.class);

    public final NumberPath<Float> avgLose = createNumber("avgLose", Float.class);

    public final NumberPath<Float> avgWin = createNumber("avgWin", Float.class);

    public final EnumPath<com.roy.football.match.OFN.response.Company> company = createEnum("company", com.roy.football.match.OFN.response.Company.class);

    public final NumberPath<Long> leagueId = createNumber("leagueId", Long.class);

    public final NumberPath<Float> pk = createNumber("pk", Float.class);

    public final EnumPath<PKType> pkType = createEnum("pkType", PKType.class);

    public QEEuroAudit(String variable) {
        super(EEuroAudit.class, forVariable(variable));
    }

    public QEEuroAudit(Path<? extends EEuroAudit> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEEuroAudit(PathMetadata<?> metadata) {
        super(EEuroAudit.class, metadata);
    }

}

