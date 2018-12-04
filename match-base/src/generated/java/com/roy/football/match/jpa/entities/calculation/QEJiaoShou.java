package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEJiaoShou is a Querydsl query type for EJiaoShou
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEJiaoShou extends EntityPathBase<EJiaoShou> {

    private static final long serialVersionUID = -1193159495L;

    public static final QEJiaoShou eJiaoShou = new QEJiaoShou("eJiaoShou");

    public final NumberPath<Float> ggoalPerMatch = createNumber("ggoalPerMatch", Float.class);

    public final NumberPath<Float> hgoalPerMatch = createNumber("hgoalPerMatch", Float.class);

    public final NumberPath<Float> latestDaxiao = createNumber("latestDaxiao", Float.class);

    public final NumberPath<Float> latestPankou = createNumber("latestPankou", Float.class);

    public final NumberPath<Integer> matchNum = createNumber("matchNum", Integer.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public final NumberPath<Float> winDrawPkRate = createNumber("winDrawPkRate", Float.class);

    public final NumberPath<Float> winDrawRate = createNumber("winDrawRate", Float.class);

    public final NumberPath<Float> winPkRate = createNumber("winPkRate", Float.class);

    public final NumberPath<Float> winRate = createNumber("winRate", Float.class);

    public QEJiaoShou(String variable) {
        super(EJiaoShou.class, forVariable(variable));
    }

    public QEJiaoShou(Path<? extends EJiaoShou> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEJiaoShou(PathMetadata<?> metadata) {
        super(EJiaoShou.class, metadata);
    }

}

