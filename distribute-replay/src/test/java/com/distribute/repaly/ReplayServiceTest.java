package com.distribute.repaly;

import com.alibaba.fastjson.JSONObject;
import com.distribute.common.response.ApiResponse;
import com.distribute.replay.ReplayApplication;
import com.distribute.replay.service.ReplayService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

/**
 * @Author renzhiqiang
 * @Description
 * @Date 2019-08-22
 **/
@SpringBootTest(classes = ReplayApplication.class)
@RunWith(SpringRunner.class)
public class ReplayServiceTest {
    @Autowired
    private ReplayService replayService;

    @Test
    public void submit() {
        String data = "hello world";
        ApiResponse response = replayService.submit(data);
        System.out.println(JSONObject.toJSONString(response));
    }
}
