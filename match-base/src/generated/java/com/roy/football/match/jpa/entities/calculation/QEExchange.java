package com.roy.football.match.jpa.entities.calculation;

import static com.mysema.query.types.PathMetadataFactory.*;

import com.mysema.query.types.path.*;

import com.mysema.query.types.PathMetadata;
import javax.annotation.Generated;
import com.mysema.query.types.Path;


/**
 * QEExchange is a Querydsl query type for EExchange
 */
@Generated("com.mysema.query.codegen.EntitySerializer")
public class QEExchange extends EntityPathBase<EExchange> {

    private static final long serialVersionUID = -838407020L;

    public static final QEExchange eExchange = new QEExchange("eExchange");

    public final NumberPath<Long> bfDrawExchange = createNumber("bfDrawExchange", Long.class);

    public final NumberPath<Float> bfDrawExgRt = createNumber("bfDrawExgRt", Float.class);

    public final NumberPath<Integer> bfDrawGain = createNumber("bfDrawGain", Integer.class);

    public final NumberPath<Long> bfLoseExchange = createNumber("bfLoseExchange", Long.class);

    public final NumberPath<Float> bfLoseExgRt = createNumber("bfLoseExgRt", Float.class);

    public final NumberPath<Integer> bfLoseGain = createNumber("bfLoseGain", Integer.class);

    public final NumberPath<Long> bfWinExchange = createNumber("bfWinExchange", Long.class);

    public final NumberPath<Float> bfWinExgRt = createNumber("bfWinExgRt", Float.class);

    public final NumberPath<Integer> bfWinGain = createNumber("bfWinGain", Integer.class);

    public final NumberPath<Long> jcDrawExchange = createNumber("jcDrawExchange", Long.class);

    public final NumberPath<Float> jcDrawExgRt = createNumber("jcDrawExgRt", Float.class);

    public final NumberPath<Integer> jcDrawGain = createNumber("jcDrawGain", Integer.class);

    public final NumberPath<Long> jcLoseExchange = createNumber("jcLoseExchange", Long.class);

    public final NumberPath<Float> jcLoseExgRt = createNumber("jcLoseExgRt", Float.class);

    public final NumberPath<Integer> jcLoseGain = createNumber("jcLoseGain", Integer.class);

    public final NumberPath<Long> jcWinExchange = createNumber("jcWinExchange", Long.class);

    public final NumberPath<Float> jcWinExgRt = createNumber("jcWinExgRt", Float.class);

    public final NumberPath<Integer> jcWinGain = createNumber("jcWinGain", Integer.class);

    public final NumberPath<Long> ofnMatchId = createNumber("ofnMatchId", Long.class);

    public QEExchange(String variable) {
        super(EExchange.class, forVariable(variable));
    }

    public QEExchange(Path<? extends EExchange> path) {
        super(path.getType(), path.getMetadata());
    }

    public QEExchange(PathMetadata<?> metadata) {
        super(EExchange.class, metadata);
    }

}

