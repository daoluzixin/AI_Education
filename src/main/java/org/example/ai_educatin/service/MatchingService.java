package org.example.ai_educatin.service;

import org.example.ai_educatin.vo.CandidateVO;

import java.util.List;

/**
 * 匹配引擎服务 - 为需求自动生成候选学生排行榜
 */
public interface MatchingService {

    /**
     * 根据需求，自动匹配候选学生列表（按匹配度评分降序）
     *
     * @param demandId 需求ID
     * @param limit    返回条数上限（默认10）
     * @return 按匹配度评分降序排列的候选学生列表
     */
    List<CandidateVO> findCandidates(Long demandId, int limit);
}
