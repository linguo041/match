package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QELeague is a Querydsl query type for ELeague
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QELeague extends EntityPathBase<ELeague> {

    private static final long serialVersionUID = 1567398112L;

    public static final QELeague eLeague = new QELeague("eLeague");

    public final EnumPath<com.roy.football.match.base.MatchContinent> continent = createEnum("continent", com.roy.football.match.base.MatchContinent.class);

    public final NumberPath<Float> goalPerMatch = createNumber("goalPerMatch", Float.class);

    public final NumberPath<Long> leagueId = createNumber("leagueId", Long.class);

    public final EnumPath<com.roy.football.match.OFN.response.Company> mainCompany = createEnum("mainCompany", com.roy.football.match.OFN.response.Company.class);

    public final StringPath name = createString("name");

    public final NumberPath<Float> netGoalPerMatch = createNumber("netGoalPerMatch", Float.class);

    public final BooleanPath state = createBoolean("state");

    public final NumberPath<Integer> teamNum = createNumber("teamNum", Integer.class);

    public QELeague(String variable) {
        super(ELeague.class, forVariable(variable));
    }

    public QELeague(Path<? extends ELeague> path) {
        super(path.getType(), path.getMetadata());
    }

    public QELeague(PathMetadata<?> metadata) {
        super(ELeague.class, metadata);
    }

}

