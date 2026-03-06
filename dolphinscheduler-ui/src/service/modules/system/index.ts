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

import { axios } from '@/service/service'

export const getSystemInfo = (): any => {
  return axios({
    url: '/system/info',
    method: 'get'
  })
}

export const updateSystemInfo = (params: { info: string }): any => {
  return axios({
    url: '/system/info/update',
    method: 'post',
    params
  })
}

export const getAgentList = (): any => {
  return axios({
    url: '/system/agent/list',
    method: 'get'
  })
}

export const updateAgentStatus = (params: { host: string; status: string }): any => {
  return axios({
    url: '/system/agent/update',
    method: 'post',
    params
  })
}

export const deleteAgent = (params: { host: string }): any => {
  return axios({
    url: '/system/agent/delete',
    method: 'delete',
    params
  })
}
