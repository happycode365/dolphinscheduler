/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.dolphinscheduler.api.service.impl;

import org.apache.dolphinscheduler.api.enums.Status;
import org.apache.dolphinscheduler.api.service.MonitorService;
import org.apache.dolphinscheduler.api.service.SystemService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.model.Server;
import org.apache.dolphinscheduler.dao.entity.User;
import org.apache.dolphinscheduler.registry.api.enums.RegistryNodeType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * system service impl
 */
@Service
@Slf4j
public class SystemServiceImpl extends BaseServiceImpl implements SystemService {

    @Autowired
    private MonitorService monitorService;

    @Override
    public Result querySystemInfo(User loginUser) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }

        Map<String, Object> systemInfo = new HashMap<>();
        systemInfo.put("version", "3.2.0-custom");
        systemInfo.put("cpuCount", Runtime.getRuntime().availableProcessors());
        systemInfo.put("totalMemory", Runtime.getRuntime().totalMemory());
        systemInfo.put("freeMemory", Runtime.getRuntime().freeMemory());
        systemInfo.put("osName", System.getProperty("os.name"));
        systemInfo.put("osVersion", System.getProperty("os.version"));

        result.setData(systemInfo);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result updateSystemInfo(User loginUser, String info) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        log.info("Update system info: {}", info);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result queryAgentStatus(User loginUser) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        List<Server> workers = monitorService.listServer(RegistryNodeType.WORKER);
        List<Map<String, Object>> agentList = new ArrayList<>();

        for (Server worker : workers) {
            Map<String, Object> agent = new HashMap<>();
            agent.put("id", worker.getId());
            agent.put("host", worker.getHost());
            agent.put("port", worker.getPort());
            agent.put("ip", worker.getHost()); // In most cases host is IP in DS
            agent.put("connectionAccount", "admin"); // Mocking connection account
            agent.put("isInstalled", true); // Mocking installation status
            agent.put("createTime", worker.getCreateTime());
            agent.put("lastHeartbeatTime", worker.getLastHeartbeatTime());
            agentList.add(agent);
        }

        result.setData(agentList);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result updateAgentStatus(User loginUser, String host, String status) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        log.info("Update agent status: host={}, status={}", host, status);
        putMsg(result, Status.SUCCESS);
        return result;
    }

    @Override
    public Result deleteAgent(User loginUser, String host) {
        Result result = new Result();
        if (!isAdmin(loginUser)) {
            putMsg(result, Status.USER_NO_OPERATION_PERM);
            return result;
        }
        log.info("Delete agent: host={}", host);
        putMsg(result, Status.SUCCESS);
        return result;
    }
}
