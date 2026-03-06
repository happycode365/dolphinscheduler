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

import { defineComponent, onMounted, ref } from 'vue'
import { NButton, NSpace, NCard, NDescriptions, NDescriptionsItem, NInput, useMessage } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { getSystemInfo, updateSystemInfo } from '@/service/modules/system'

const InfoManage = defineComponent({
  name: 'SystemInfoManage',
  setup() {
    const { t } = useI18n()
    const message = useMessage()
    const systemInfo = ref<any>({})
    const editInfo = ref('')

    const loadInfo = async () => {
      const res = await getSystemInfo()
      systemInfo.value = res
      editInfo.value = JSON.stringify(res, null, 2)
    }

    const handleUpdate = async () => {
      await updateSystemInfo({ info: editInfo.value })
      message.success(t('system.info.update_success'))
      loadInfo()
    }

    onMounted(() => {
      loadInfo()
    })

    return {
      t,
      systemInfo,
      editInfo,
      handleUpdate
    }
  },
  render() {
    return (
      <NSpace vertical>
        <NCard title={this.t('menu.system_info_manage')}>
          <NDescriptions bordered label-placement='left' column={2}>
            <NDescriptionsItem label='Version'>{this.systemInfo.version}</NDescriptionsItem>
            <NDescriptionsItem label='CPU Count'>{this.systemInfo.cpuCount}</NDescriptionsItem>
            <NDescriptionsItem label='Total Memory'>{this.systemInfo.totalMemory}</NDescriptionsItem>
            <NDescriptionsItem label='Free Memory'>{this.systemInfo.freeMemory}</NDescriptionsItem>
            <NDescriptionsItem label='OS Name'>{this.systemInfo.osName}</NDescriptionsItem>
            <NDescriptionsItem label='OS Version'>{this.systemInfo.osVersion}</NDescriptionsItem>
          </NDescriptions>
        </NCard>
        <NCard title='Update System Info'>
          <NSpace vertical>
            <NInput
              v-model:value={this.editInfo}
              type='textarea'
              placeholder='Enter system info in JSON format'
              rows={10}
            />
            <NButton type='primary' onClick={this.handleUpdate}>
              Update
            </NButton>
          </NSpace>
        </NCard>
      </NSpace>
    )
  }
})

export default InfoManage
