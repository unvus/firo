package com.unvus.firo.embedded.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.JPQLQuery;
import com.querydsl.sql.SQLQueryFactory;
import com.unvus.firo.embedded.domain.FiroFile;
import com.unvus.firo.embedded.domain.QFiroFile;
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport;
import org.springframework.util.CollectionUtils;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class FiroRepositoryImpl extends QuerydslRepositorySupport implements FiroRepositoryCustom {

    private final SQLQueryFactory queryFactory;

    /**
     * Creates a new {@link QuerydslRepositorySupport} instance for the given domain type.
     * @param queryFactory
     */
    public FiroRepositoryImpl(SQLQueryFactory queryFactory) {
        super(FiroFile.class);
        this.queryFactory = queryFactory;
    }

    @Override
    public List<FiroFile> listAttach(Map<String, Object> params) {
        QFiroFile firoFile = QFiroFile.firoFile;
        JPQLQuery query = from(firoFile);
        BooleanBuilder builder = getBooleanBuilder(params, firoFile);

        query.where(builder);
        return null;
    }

    private BooleanBuilder getBooleanBuilder(Map params, QFiroFile firoFile) {
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(referer.refererName.isNotNull());

        if(queryMap.containsKey("nodes")) {
            List<Map<String, Integer>> nodes = (List<Map<String, Integer>>)queryMap.get("nodes");
            if(!CollectionUtils.isEmpty(nodes)) {
                List<Long> nodeIds = new ArrayList<>();
                for(Map<String, Integer> node : nodes) {
                    nodeIds.add(node.get("id").longValue());
                }
                builder.and(referer.node.id.in(nodeIds));
            }
        }

        if(queryMap.containsKey("modules")) {
            List<Map<String, Integer>> modules = (List<Map<String, Integer>>)queryMap.get("modules");
            if(!CollectionUtils.isEmpty(modules)) {
                List<Long> moduleIds = new ArrayList<>();
                for(Map<String, Integer> module : modules) {
                    moduleIds.add(module.get("id").longValue());
                }
                builder.and(referer.module.id.in(moduleIds));
            }
        }

        Map<String, Object> toMap = (Map<String, Object>)queryMap.get("to");
        ZonedDateTime to = DateUtils.convert((Long) toMap.get("date"), DateUtils.ConvertTo.ZonedDateTime);
        to = to.plusDays(1).toLocalDate().atStartOfDay(ZoneOffset.UTC);
        builder.and(referer.createdDate.loe(to));

        Map<String, Object> fromMap = (Map<String, Object>)queryMap.get("from");
        ZonedDateTime from = DateUtils.convert((Long) fromMap.get("date"), DateUtils.ConvertTo.ZonedDateTime);
        from = from.toLocalDate().atStartOfDay(ZoneOffset.UTC);
        builder.and(referer.createdDate.goe(from));
        return builder;
    }
}
