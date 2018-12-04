package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QETeam is a Querydsl query type for ETeam
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QETeam extends EntityPathBase<ETeam> {

    private static final long serialVersionUID = -592543410L;

    public static final QETeam eTeam = new QETeam("eTeam");

    public final StringPath city = createString("city");

    public final NumberPath<Long> cityTeamId1 = createNumber("cityTeamId1", Long.class);

    public final NumberPath<Long> cityTeamId2 = createNumber("cityTeamId2", Long.class);

    public final StringPath contry = createString("contry");

    public final StringPath enTeamName = createString("enTeamName");

    public final StringPath field = createString("field");

    public final StringPath ftName = createString("ftName");

    public final NumberPath<Integer> ftPoint = createNumber("ftPoint", Integer.class);

    public final BooleanPath national = createBoolean("national");

    public final NumberPath<Long> teamId = createNumber("teamId", Long.class);

    public final StringPath teamName = createString("teamName");

    public QETeam(String variable) {
        super(ETeam.class, forVariable(variable));
    }

    public QETeam(Path<? extends ETeam> path) {
        super(path.getType(), path.getMetadata());
    }

    public QETeam(PathMetadata<?> metadata) {
        super(ETeam.class, metadata);
    }

}

