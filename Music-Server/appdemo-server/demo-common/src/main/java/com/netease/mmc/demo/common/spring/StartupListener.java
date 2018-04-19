package com.netease.mmc.demo.common.spring;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import com.netease.mmc.demo.common.context.WebContextHolder;

/**
 * Spring启动监听类.
 *
 * @author hzwanglin1
 * @date 2017/7/11
 * @since 1.0
 */
@Component
public class StartupListener implements ApplicationListener<ContextRefreshedEvent> {
    @Value("${profile.id}")
    private String profileId;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // 设置当前环境id到全局上下文中
        WebContextHolder.setProfileId(profileId);
    }
}
