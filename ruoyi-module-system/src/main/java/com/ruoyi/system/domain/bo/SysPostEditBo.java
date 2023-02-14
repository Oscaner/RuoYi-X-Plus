package com.ruoyi.system.domain.bo;

import com.ruoyi.common.enums.CommonNormalDisable;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 岗位信息修改业务对象
 *
 * @author weibocy
 */

@Data
@Schema(description = "岗位信息修改业务对象")
public class SysPostEditBo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 岗位序号
     */
    @Schema(description = "岗位序号", required = true)
    private Long postId;

    /**
     * 岗位编码
     */
    @Schema(description = "岗位编码", required = true)
    @Size(min = 0, max = 64, message = "岗位编码长度不能超过{max}个字符")
    private String postCode;

    /**
     * 岗位名称
     */
    @Schema(description = "岗位名称", required = true)
    @Size(min = 1, max = 50, message = "岗位名称长度不能超过{max}个字符")
    private String postName;

    /**
     * 岗位排序
     */
    @Schema(description = "岗位排序", required = true)
    @NotNull(message = "显示顺序不能为空")
    private Integer postSort;

    /**
     * 状态（NORMAL=正常 DISABLE=停用）
     */
    @Schema(description = "状态（NORMAL=正常 DISABLE=停用）", required = true)
    @NotBlank(message = "状态不能为空")
    private CommonNormalDisable status;

    /**
     * 备注
     */
    @Schema(description = "备注")
    private String remark;


}
