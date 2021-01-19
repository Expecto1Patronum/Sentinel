/*
 * Copyright 1999-2018 Alibaba Group Holding Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.alibaba.csp.sentinel.dashboard.rule.apollo.system;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRulePublisher;
import com.alibaba.csp.sentinel.dashboard.rule.apollo.ApolloConfigUtil;
import com.alibaba.csp.sentinel.dashboard.rule.nacos.NacosConfigUtil;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author hbj
 */
@Component("systemRuleApolloPublisher")
@ConditionalOnProperty(name = "enable.rule.persistence", havingValue = "apollo")
public class SystemRuleApolloPublisher implements DynamicRulePublisher<List<ParamFlowRuleEntity>> {
    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Override
    public void publish(String app, List<ParamFlowRuleEntity> rules) throws Exception {
        ApolloConfigUtil.setRuleStringToApollo(
                this.apolloOpenApiClient,
                app,
                NacosConfigUtil.SYSTEM_DATA_ID_POSTFIX,
                rules
        );
    }
}
