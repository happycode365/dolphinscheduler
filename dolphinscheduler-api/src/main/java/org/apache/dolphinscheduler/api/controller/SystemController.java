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

package org.apache.dolphinscheduler.api.controller;

import org.apache.dolphinscheduler.api.service.SystemService;
import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.common.constants.Constants;
import org.apache.dolphinscheduler.dao.entity.User;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * system controller
 */
@Tag(name = "SYSTEM_TAG")
@RestController
@RequestMapping("/system")
public class SystemController extends BaseController {

    @Autowired
    private SystemService systemService;

    @Operation(summary = "querySystemInfo", description = "QUERY_SYSTEM_INFO")
    @GetMapping(value = "/info")
    @ResponseStatus(HttpStatus.OK)
    public Result querySystemInfo(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return systemService.querySystemInfo(loginUser);
    }

    @Operation(summary = "updateSystemInfo", description = "UPDATE_SYSTEM_INFO")
    @PostMapping(value = "/info/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateSystemInfo(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("info") String info) {
        return systemService.updateSystemInfo(loginUser, info);
    }

    @Operation(summary = "queryAgentStatus", description = "QUERY_AGENT_STATUS")
    @GetMapping(value = "/agent/list")
    @ResponseStatus(HttpStatus.OK)
    public Result queryAgentStatus(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser) {
        return systemService.queryAgentStatus(loginUser);
    }

    @Operation(summary = "updateAgentStatus", description = "UPDATE_AGENT_STATUS")
    @PostMapping(value = "/agent/update")
    @ResponseStatus(HttpStatus.OK)
    public Result updateAgentStatus(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                                   @RequestParam("host") String host,
                                   @RequestParam("status") String status) {
        return systemService.updateAgentStatus(loginUser, host, status);
    }

    @Operation(summary = "deleteAgent", description = "DELETE_AGENT")
    @DeleteMapping(value = "/agent/delete")
    @ResponseStatus(HttpStatus.OK)
    public Result deleteAgent(@Parameter(hidden = true) @RequestAttribute(value = Constants.SESSION_USER) User loginUser,
                              @RequestParam("host") String host) {
        return systemService.deleteAgent(loginUser, host);
    }
}
