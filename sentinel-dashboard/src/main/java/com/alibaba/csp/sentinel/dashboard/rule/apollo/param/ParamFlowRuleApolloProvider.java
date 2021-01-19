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
package com.alibaba.csp.sentinel.dashboard.rule.apollo.param;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.ParamFlowRuleEntity;
import com.alibaba.csp.sentinel.dashboard.rule.DynamicRuleProvider;
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
@Component("paramFlowRuleApolloProvider")
@ConditionalOnProperty(name = "enable.rule.persistence", havingValue = "apollo")
public class ParamFlowRuleApolloProvider implements DynamicRuleProvider<List<ParamFlowRuleEntity>> {

    @Autowired
    private ApolloOpenApiClient apolloOpenApiClient;

    @Override
    public List<ParamFlowRuleEntity> getRules(String appName) throws Exception {
        return ApolloConfigUtil.getRuleEntitiesFromApollo(
                this.apolloOpenApiClient,
                appName,
                NacosConfigUtil.PARAM_FLOW_DATA_ID_POSTFIX,
                ParamFlowRuleEntity.class
        );
    }
}
