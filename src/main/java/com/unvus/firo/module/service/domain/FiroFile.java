package com.unvus.firo.module.service.domain;

import lombok.Data;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "unvus_firo")
public class FiroFile {

    // Raw attributes
    /**
     * 파일ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "firo_id", length = 19, nullable = false)
    protected Long id;

    /**
     * 참조구분
     */
    @NotNull
    @Size(min = 1, max = 50)
    @Column(name = "firo_ref_target", length = 50, nullable = false)
    protected String refTarget;

    /**
     * 참조구분키
     */
    @NotNull
    @Column(name = "firo_ref_target_key", length = 19, nullable = false)
    protected Long refTargetKey;

    /**
     * 참조타입
     */
    @Column(name = "firo_ref_target_type", length = 50)
    protected String refTargetType;

    /**
     * 표시파일명
     */
    @NotNull
    @Column(name = "firo_display_name", length = 500, nullable = false)
    protected String displayName;

    /**
     * 저장파일명
     */
    @NotNull
    @Column(name = "firo_saved_name", length = 200, nullable = false)
    protected String savedName;

    /**
     * 파일 설명
     */
    @Column(name = "firo_desc", length = 4000)
    protected String description;

    /**
     * 저장경로
     */
    @NotNull
    @Column(name = "firo_saved_dir", length = 500, nullable = false)
    protected String savedDir;

    /**
     * 파일타입
     */
    @Column(name = "firo_file_type", length = 50)
    protected String fileType;

    /**
     * 파일사이즈
     */
    @NotNull
    @Column(name = "firo_file_size", length = 19, nullable = false)
    protected Long fileSize;

    /**
     * access 횟수
     */
    @NotNull
    @Column(name = "firo_access_cnt", length = 9, nullable = false)
    protected Integer accessCnt;

    /**
     * 삭제여부
     */
    @NotNull
    @Column(name = "firo_deleted", nullable = false)
    protected Boolean deleted = false;

    /**
     * 확장컬럼
     */
    @Column(name = "firo_ext", length = 2000)
    protected String ext;

    /**
     * 등록자
     */
    @Column(name = "firo_created_by")
    protected Long createdBy;

    /**
     * 등록일시
     */
    @Column(name = "firo_created_dt")
    protected LocalDateTime createdDt;

//    /**
//     * 수정자
//     */
//    @Column(name = "firo_modified_by")
//    protected Long modifiedBy;
//
//    /**
//     * 수정일시
//     */
//    @Column(name = "firo_modified_dt")
//    protected LocalDateTime modifiedDt;

}
