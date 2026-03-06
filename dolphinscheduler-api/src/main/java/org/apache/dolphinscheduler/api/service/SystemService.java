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

package org.apache.dolphinscheduler.api.service;

import org.apache.dolphinscheduler.api.utils.Result;
import org.apache.dolphinscheduler.dao.entity.User;

/**
 * system service
 */
public interface SystemService {

    /**
     * query system info
     *
     * @param loginUser login user
     * @return system info
     */
    Result querySystemInfo(User loginUser);

    /**
     * update system info
     *
     * @param loginUser login user
     * @param info system info
     * @return update result
     */
    Result updateSystemInfo(User loginUser, String info);

    /**
     * query agent status
     *
     * @param loginUser login user
     * @return agent status
     */
    Result queryAgentStatus(User loginUser);

    /**
     * update agent status
     *
     * @param loginUser login user
     * @param host host
     * @param status status
     * @return update result
     */
    Result updateAgentStatus(User loginUser, String host, String status);

    /**
     * delete agent
     *
     * @param loginUser login user
     * @param host host
     * @return delete result
     */
    Result deleteAgent(User loginUser, String host);
}
