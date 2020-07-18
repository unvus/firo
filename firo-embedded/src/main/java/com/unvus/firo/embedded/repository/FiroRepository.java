package com.unvus.firo.embedded.repository;

import com.unvus.firo.embedded.domain.FiroFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * https://stackoverflow.com/questions/46742322/query-creation-using-dynamic-filter-map-in-querydsl-and-spring-data-jpa
 */
@Repository
public interface FiroRepository extends JpaRepository<FiroFile, Long>, FiroRepositoryCustom {

}
