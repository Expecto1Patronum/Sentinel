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
package com.alibaba.csp.sentinel.dashboard.rule.apollo;

import com.alibaba.csp.sentinel.dashboard.datasource.entity.rule.RuleEntity;
import com.alibaba.csp.sentinel.dashboard.util.JSONUtils;
import com.alibaba.csp.sentinel.slots.block.Rule;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.nacos.api.exception.NacosException;
import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author hantianwei@gmail.com
 * @since 1.5.0
 */
public final class ApolloConfigUtil {
    public static final String FLOW_DATA_ID_POSTFIX = "-flow-rules";
    public static final String DEGRADE_DATA_ID_POSTFIX = "-degrade-rules";
    public static final String SYSTEM_DATA_ID_POSTFIX = "-system-rules";
    public static final String PARAM_FLOW_DATA_ID_POSTFIX = "-param-rules";
    public static final String AUTHORITY_DATA_ID_POSTFIX = "-authority-rules";
    public static final String DASHBOARD_POSTFIX = "-dashboard";

    private ApolloConfigUtil() {
    }

    public static String genDataId(String appName, String postfix) {
        return String.format("%s%s", appName, postfix);
    }

    public static <T> void setRuleStringToApollo(ApolloOpenApiClient apolloOpenApiClient, String app, String postfix, List<T> rules) throws NacosException {
        // Increase the configuration
        String appId = "appId";
        String dataId = ApolloConfigUtil.genDataId(app, postfix);
        List<Rule> ruleForApp = rules.stream()
                .map(rule -> {
                    RuleEntity rule1 = (RuleEntity) rule;
                    return rule1.toRule();
                })
                .collect(Collectors.toList());
        OpenItemDTO openItemDTO = new OpenItemDTO();
        openItemDTO.setKey(dataId);
        openItemDTO.setValue(JSONUtils.toJSONString(ruleForApp));
        openItemDTO.setComment("Program auto-join");
        openItemDTO.setDataChangeCreatedBy("some-operator");
        apolloOpenApiClient.createOrUpdateItem(appId, "DEV", "default", "application", openItemDTO);

        OpenItemDTO openItemDashboardDTO = new OpenItemDTO();
        openItemDashboardDTO.setKey(dataId + DASHBOARD_POSTFIX);
        openItemDashboardDTO.setValue(JSONUtils.toJSONString(rules));
        openItemDashboardDTO.setComment("Program auto-join");
        openItemDashboardDTO.setDataChangeCreatedBy("some-operator");
        apolloOpenApiClient.createOrUpdateItem(appId, "DEV", "default", "application", openItemDTO);
    }

    public static <T> List<T> getRuleEntitiesFromApollo(ApolloOpenApiClient apolloOpenApiClient, String appName, String postfix, Class<T> clazz) {
        String appId = "appId";
        String dataId = ApolloConfigUtil.genDataId(appName, postfix) + DASHBOARD_POSTFIX;
        OpenNamespaceDTO openNamespaceDTO = apolloOpenApiClient.getNamespace(appId, "DEV", "default", "application");
        String rules = openNamespaceDTO
                .getItems()
                .stream()
                .filter(p -> p.getKey().equals(dataId))
                .map(OpenItemDTO::getValue)
                .findFirst()
                .orElse("");

        if (StringUtil.isEmpty(rules)) {
            return new ArrayList<>();
        }
        return JSONUtils.parseObject(clazz, rules);
    }
}
