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
public interface FiroRepository extends JpaRepository<FiroFile, Long>  {

    @Query(
        "SELECT COUNT(f) FROM FiroFile f " +
        " WHERE f.refTarget=:refTarget " +
        "   AND  f.refTargetKey=:refTargetKey " +
        "   AND  f.refTargetType=:refTargetType")
    int listAttachCnt(@Param("refTarget") String refTarget,
                      @Param("refTargetKey") Long refTargetKey,
                      @Param("refTargetType") String refTargetType);



    /**
     * if yuou want to avoid result objects less than dataPerPage caused by "1:N mappings".. <br/>
     * add function that return "distinct id list" and name it with : <br/>
     * <code>List<Long> listAttachIds(Map<String, Object> params)</code> <br/>
     * and replace @Pageable with :  <br/>
     * <code>@Pageable(useMergeQuery = true)</code> <br/>
     */
    List<FiroFile> listAttach(Map<String, Object> params);

    List<Long> listAttachIds(Map<String, Object> params);

    int markAsDelete(Long id);


    int insertAttach(FiroFile attach);

    int updateAttach(FiroFile attach);
}
