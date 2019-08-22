package com.distribute.replay.service;

import com.distribute.common.response.ApiResponse;
import com.distribute.replay.base.annotation.Replay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * @Author renzhiqiang
 * @Description 重发请求测试controller
 * @Date 2019-08-22
 **/
@Service
public class ReplayService {
    private Logger logger = LoggerFactory.getLogger(ReplayService.class);

    /**
     * 提交数据到后台
     * @param data
     * @return
     */
    @Replay(enable = false, expireTime = 60 * 1000L)
    public ApiResponse<Boolean> submit(String data) {
        // 处理提交的数据
        logger.info("recv data: {}", data);
        return ApiResponse.buildSucess(true);
    }
}
