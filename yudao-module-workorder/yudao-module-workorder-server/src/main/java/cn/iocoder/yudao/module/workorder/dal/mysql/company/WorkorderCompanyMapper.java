package cn.iocoder.yudao.module.workorder.dal.mysql.company;

import cn.iocoder.yudao.framework.common.pojo.PageResult;
import cn.iocoder.yudao.framework.mybatis.core.mapper.BaseMapperX;
import cn.iocoder.yudao.framework.mybatis.core.query.LambdaQueryWrapperX;
import cn.iocoder.yudao.module.workorder.controller.admin.company.vo.WorkorderCompanyPageReqVO;
import cn.iocoder.yudao.module.workorder.dal.dataobject.company.WorkorderCompanyDO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WorkorderCompanyMapper extends BaseMapperX<WorkorderCompanyDO> {
    default PageResult<WorkorderCompanyDO> selectPage(WorkorderCompanyPageReqVO reqVO) {
        return selectPage(reqVO, new LambdaQueryWrapperX<WorkorderCompanyDO>()
                .likeIfPresent(WorkorderCompanyDO::getName, reqVO.getName())
                .eqIfPresent(WorkorderCompanyDO::getStatus, reqVO.getStatus())
                .betweenIfPresent(WorkorderCompanyDO::getCreateTime, reqVO.getCreateTime())
                .orderByDesc(WorkorderCompanyDO::getId));
    }

    /**
     * 根据状态查询所有企业列表
     *
     * @param status 状态，可选。如果为空则查询所有状态的企业
     */
    default List<WorkorderCompanyDO> selectListByStatus(Integer status) {
        return selectList(new LambdaQueryWrapperX<WorkorderCompanyDO>()
                .eqIfPresent(WorkorderCompanyDO::getStatus, status)
                .orderByAsc(WorkorderCompanyDO::getId));
    }
}


