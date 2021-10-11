package com.unvus.firo.module.service.repository;

import com.unvus.firo.module.service.domain.FiroFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * https://stackoverflow.com/questions/46742322/query-creation-using-dynamic-filter-map-in-querydsl-and-spring-data-jpa
 */
@Repository
public interface FiroRepository extends JpaRepository<FiroFile, Long>, FiroRepositoryCustom {

}
