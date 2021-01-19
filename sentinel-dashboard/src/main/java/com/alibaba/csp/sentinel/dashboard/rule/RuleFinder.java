package com.alibaba.csp.sentinel.dashboard.rule;

import org.jetbrains.annotations.NotNull;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

/**
 * @author hbj
 * @date 2021/1/19 5:03 下午
 */
@Component
public class RuleFinder<T> implements ApplicationContextAware {
    @Value("${enable.rule.persistence}")
    private String thirdStore;

    private final List<String> thirdStoreList = Arrays.asList("Nacos", "Apollo");

    public DynamicRuleProvider<List<T>> findProvider(String ruleName) {
        if (!thirdStoreList.contains(thirdStore)) {
            throw new RuntimeException("未能找到正确的配置中心");
        }
        switch (ruleName) {
            case "authorityRule":
            case "degradeRule":
            case "flowRule":
            case "paramFlowRule":
            case "systemRule":
                return (DynamicRuleProvider<List<T>>) RuleFinder.getBean(ruleName + thirdStore + "Provider");
            default:
                return null;
        }
    }

    public DynamicRulePublisher<List<T>> findPublisher(String ruleName) {
        if (!thirdStoreList.contains(thirdStore)) {
            throw new RuntimeException("未能找到正确的配置中心");
        }
        switch (ruleName) {
            case "authorityRule":
            case "degradeRule":
            case "flowRule":
            case "paramFlowRule":
            case "systemRule":
                return (DynamicRulePublisher<List<T>>) RuleFinder.getBean(ruleName + thirdStore + "Publisher");
            default:
                return null;
        }
    }

    private static ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(@NotNull ApplicationContext applicationContext) throws BeansException {
        RuleFinder.applicationContext = applicationContext;
    }

    public static Object getBean(String beanName) {
        return applicationContext != null ? applicationContext.getBean(beanName) : null;
    }
}
