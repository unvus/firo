package com.unvus.firo.embedded.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.unvus.firo.embedded.domain.FiroFile;
import com.unvus.firo.embedded.domain.QFiroFile;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;

import java.util.List;
import java.util.Map;

public class FiroRepositoryImpl extends QuerydslRepositorySupport implements FiroRepositoryCustom {

    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     * @param
     */
    public FiroRepositoryImpl() {
        super(FiroFile.class);
    }

    @Override
    public List<FiroFile> listAttach(Map<String, Object> params) {
        QFiroFile firoFile = new QFiroFile("ff");
        JPQLQuery query = from(firoFile);
        BooleanBuilder builder = getBooleanBuilder(params);

        query.where(builder);
        return query.fetch();
    }

    @Override
    public long listAttachCnt(Map<String, Object> params) {
        QFiroFile firoFile = new QFiroFile("ff");
        JPQLQuery query = from(firoFile);
        BooleanBuilder builder = getBooleanBuilder(params);

        query.where(builder);
        return query.fetchCount();
    }

    private BooleanBuilder getBooleanBuilder(Map<String, Object> params) {
        BooleanBuilder builder = new BooleanBuilder();

        PathBuilder<FiroFile> path = new PathBuilder<>(FiroFile.class, "ff");
        if(params != null && !params.isEmpty()) {
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                builder.and(path.get(entry.getKey()).eq(entry.getValue()));
            }
        }

        return builder;
    }
}
