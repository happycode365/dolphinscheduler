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
import { NButton, NSpace, NDataTable, NPopconfirm, useMessage, NTag } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { getAgentList, updateAgentStatus, deleteAgent } from '@/service/modules/system'
import Card from '@/components/card'

const AgentStatus = defineComponent({
  name: 'SystemAgentStatus',
  setup() {
    const { t } = useI18n()
    const message = useMessage()
    const loading = ref(false)
    const list = ref([])

    const loadList = async () => {
      loading.value = true
      try {
        const res = await getAgentList()
        list.value = res
      } finally {
        loading.value = false
      }
    }

    const handleUpdateStatus = async (host: string, status: string) => {
      await updateAgentStatus({ host, status })
      message.success('Update Success')
      loadList()
    }

    const handleDelete = async (host: string) => {
      await deleteAgent({ host })
      message.success('Delete Success')
      loadList()
    }

    onMounted(() => {
      loadList()
    })

    const columns = [
      { title: 'IP', key: 'ip' },
      { title: 'Port', key: 'port' },
      { title: 'Account', key: 'connectionAccount' },
      {
        title: 'Installed',
        key: 'isInstalled',
        render: (row: any) => (
          <NTag type={row.isInstalled ? 'success' : 'error'}>
            {row.isInstalled ? 'Installed' : 'Not Installed'}
          </NTag>
        )
      },
      { title: 'Create Time', key: 'createTime' },
      { title: 'Last Heartbeat', key: 'lastHeartbeatTime' },
      {
        title: 'Action',
        key: 'actions',
        render: (row: any) => (
          <NSpace>
            <NButton size='small' onClick={() => handleUpdateStatus(row.host, 'NORMAL')}>
              Normal
            </NButton>
            <NButton size='small' type='warning' onClick={() => handleUpdateStatus(row.host, 'ABNORMAL')}>
              Abnormal
            </NButton>
            <NPopconfirm onPositiveClick={() => handleDelete(row.host)}>
              {{
                trigger: () => <NButton size='small' type='error'>Delete</NButton>,
                default: () => 'Are you sure to delete this agent?'
              }}
            </NPopconfirm>
          </NSpace>
        )
      }
    ]

    return {
      t,
      list,
      loading,
      columns
    }
  },
  render() {
    return (
      <Card title={this.t('menu.agent_status_manage')}>
        <NDataTable
          loading={this.loading}
          columns={this.columns}
          data={this.list}
        />
      </Card>
    )
  }
})

export default AgentStatus
