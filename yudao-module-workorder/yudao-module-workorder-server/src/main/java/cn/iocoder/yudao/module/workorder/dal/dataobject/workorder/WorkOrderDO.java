package cn.iocoder.yudao.module.workorder.dal.dataobject.workorder;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.util.*;
import java.time.LocalDateTime;
import java.time.LocalDateTime;
import com.baomidou.mybatisplus.annotation.*;
import cn.iocoder.yudao.framework.mybatis.core.dataobject.BaseDO;

/**
 * 工单信息 DO
 *
 * @author 芋道源码
 */
@TableName("report_work_order")
@KeySequence("report_work_order_seq") // 用于 Oracle、PostgreSQL、Kingbase、DB2、H2 数据库的主键自增。如果是 MySQL 等数据库，可不写。
@Data
@EqualsAndHashCode(callSuper = true)
@ToString(callSuper = true)
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WorkOrderDO extends BaseDO {

    /**
     * 工单ID
     */
    @TableId
    private Long id;
    /**
     * 工单名称
     */
    private String name;
    /**
     * 备注
     */
    private String remark;
    /**
     * 区域ID
     */
    private Long areaId;
    /**
     * 标签ID列表，多个用逗号分隔
     *
     * 枚举 {@link TODO tag_type 对应的类}
     */
    private String tagIds;
    /**
     * 工单状态：1-待处理 2-待审核 3-审核完成 4-审核失败 5-通知完成 6-通知失败
     *
     * 枚举 {@link TODO work_order_status 对应的类}
     */
    private Integer status;

    /**
     * 文件类型：1-PDF 2-DOC 3-XLS 4-PIC
     *
     * 枚举 {@link TODO file_type 对应的类}
     */
    private Integer fileType;

    /**
     * 文件地址
     */
    private String fileUrl;


}





