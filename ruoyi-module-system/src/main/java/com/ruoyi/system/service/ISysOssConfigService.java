package com.ruoyi.system.service;

import com.ruoyi.common.core.domain.bo.PageQuery;
import com.ruoyi.common.core.page.TableDataInfo;
import com.ruoyi.system.domain.bo.SysOssConfigEditBo;
import com.ruoyi.system.domain.bo.SysOssConfigQueryBo;
import com.ruoyi.system.domain.vo.SysOssConfigVo;

import java.util.Collection;

/**
 * 对象存储配置Service接口
 *
 * @author weibocy
 */
public interface ISysOssConfigService {

    /**
     * 初始化OSS配置
     */
    void init();

    /**
     * 查询单个
     */
    SysOssConfigVo queryById(Long ossConfigId);

    /**
     * 查询列表
     */
    TableDataInfo<SysOssConfigVo> queryPageList(SysOssConfigQueryBo ossConfigQuery, PageQuery pageQuery);


    /**
     * 根据新增业务对象插入对象存储配置
     *
     * @param bo 对象存储配置新增业务对象
     * @return
     */
    Boolean insertByBo(SysOssConfigEditBo bo);

    /**
     * 根据编辑业务对象修改对象存储配置
     *
     * @param bo 对象存储配置编辑业务对象
     * @return
     */
    Boolean updateByBo(SysOssConfigEditBo bo);

    /**
     * 校验并删除数据
     *
     * @param ids     主键集合
     * @param isValid 是否校验,true-删除前校验,false-不校验
     * @return
     */
    Boolean deleteWithValidByIds(Collection<Long> ids, Boolean isValid);

    /**
     * 启用停用状态
     */
    int updateOssConfigStatus(SysOssConfigEditBo bo);

}
