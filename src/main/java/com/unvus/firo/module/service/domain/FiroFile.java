package com.unvus.firo.module.service.domain;

import lombok.Data;
import org.hibernate.annotations.Nationalized;
import org.hibernate.annotations.Proxy;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;

@Proxy(lazy = false)
@Data
@Entity
@Table(name = "cms_attach")
public class FiroFile {

    // Raw attributes
    /**
     * 파일ID
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attach_id", length = 19)
    protected Long id;

    /**
     * 참조구분
     */
    @NotNull
    @Size(max = 100)
    @Nationalized
    @Column(name = "attach_ref_target")
    protected String refTarget;

    /**
     * 참조구분키
     */
    @NotNull
    @Column(name = "attach_ref_target_key", length = 19)
    protected Long refTargetKey;

    /**
     * 참조타입
     */
    @Size(max = 100)
    @Column(name = "attach_ref_target_type")
    protected String refTargetType;

    /**
     * 표시파일명
     */
    @NotNull
    @Size(max = 300)
    @Nationalized
    @Column(name = "attach_display_name")
    protected String displayName;

    /**
     * 저장파일명
     */
    @NotNull
    @Size(max = 200)
    @Nationalized
    @Column(name = "attach_saved_name")
    protected String savedName;

    /**
     * 저장경로
     */
    @NotNull
    @Size(max = 150)
    @Column(name = "attach_saved_dir")
    protected String savedDir;

    /**
     * 파일타입
     */
    @NotNull
    @Size(max = 100)
    @Nationalized
    @Column(name = "attach_file_type")
    protected String fileType;

    /**
     * 파일사이즈
     */
    @Column(name = "attach_file_size", length = 19)
    protected Long fileSize;

    /**
     * 삭제여부
     */
    @NotNull
    @Column(name = "attach_deleted")
    protected Boolean deleted = false;

    /**
     * 확장컬럼
     */
    @Size(max = 4000)
    @Nationalized
    @Column(name = "attach_ext")
    protected String ext;

    /**
     * 등록자
     */
    @Size(max = 50)
    @Column(name = "attach_created_by")
    protected String createdBy;

    /**
     * 등록일시
     */
    @NotNull
    @Column(name = "attach_created_dt")
    protected LocalDateTime createdDt;

    @Transient
    protected String directUrl;

    @Override
    public String toString() {
        return "FiroFile{" +
            "id=" + id +
            ", refTarget='" + refTarget + '\'' +
            ", refTargetKey=" + refTargetKey +
            ", refTargetType='" + refTargetType + '\'' +
            ", displayName='" + displayName + '\'' +
            ", savedName='" + savedName + '\'' +
            ", savedDir='" + savedDir + '\'' +
            ", fileType='" + fileType + '\'' +
            ", fileSize=" + fileSize +
            ", deleted=" + deleted +
            ", ext='" + ext + '\'' +
            ", createdBy='" + createdBy + '\'' +
            ", createdDt=" + createdDt +
            '}';
    }
//    /**
//     * 수정자
//     */
//    @Column(name = "modified_by")
//    protected Long modifiedBy;
//
//    /**
//     * 수정일시
//     */
//    @Column(name = "modified_dt")
//    protected LocalDateTime modifiedDt;

}
