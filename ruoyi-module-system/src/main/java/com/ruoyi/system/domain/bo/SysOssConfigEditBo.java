package com.ruoyi.system.domain.bo;

import com.ruoyi.common.core.validate.EditGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.io.Serializable;

/**
 * 对象存储配置修改业务对象
 *
 * @author weibocy
 */

@Data
@ApiModel(value = "SysOssConfigEditBo", description = "对象存储配置修改业务对象")
public class SysOssConfigEditBo implements Serializable {

    private static final long serialVersionUID=1L;

    /**
     * 主建
     */
    @ApiModelProperty(value = "主建", required = true)
    @NotNull(message = "主建不能为空", groups = {EditGroup.class})
    private Long ossConfigId;

    /**
     * 配置key
     */
    @ApiModelProperty(value = "配置key")
    @Size(min = 2, max = 100, message = "configKey长度必须介于2和20 之间")
    private String configKey;

    /**
     * accessKey
     */
    @ApiModelProperty(value = "accessKey")
    @Size(min = 2, max = 100, message = "accessKey长度必须介于2和100 之间")
    private String accessKey;

    /**
     * 秘钥
     */
    @ApiModelProperty(value = "secretKey")
    @Size(min = 2, max = 100, message = "secretKey长度必须介于2和100 之间")
    private String secretKey;

    /**
     * 桶名称
     */
    @ApiModelProperty(value = "桶名称")
    @Size(min = 2, max = 100, message = "bucketName长度必须介于2和100之间")
    private String bucketName;

    /**
     * 前缀
     */
    @ApiModelProperty(value = "前缀")
    private String prefix;

    /**
     * 访问站点
     */
    @ApiModelProperty(value = "访问站点")
    @Size(min = 2, max = 100, message = "endpoint长度必须介于2和100之间")
    private String endpoint;

    /**
     * 自定义域名
     */
    @ApiModelProperty("自定义域名")
    private String domain;

    /**
     * 是否https（Y=是,N=否）
     */
    @ApiModelProperty("是否https（Y=是,N=否）")
    private String isHttps;

    /**
     * 状态（0=正常,1=停用）
     */
    @ApiModelProperty("状态（0=正常,1=停用）")
    private String status;

    /**
     * 域
     */
    @ApiModelProperty(value = "域")
    private String region;

    /**
     * 扩展字段
     */
    @ApiModelProperty(value = "扩展字段")
    private String ext1;

    /**
     * 备注
     */
    @ApiModelProperty(value = "备注")
    private String remark;

}
