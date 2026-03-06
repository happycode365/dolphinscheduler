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
import { NButton, NSpace, NDataTable, NCheckbox, NCheckboxGroup, useMessage } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import Card from '@/components/card'

const PermissionManage = defineComponent({
  name: 'SystemPermissionManage',
  setup() {
    const { t } = useI18n()
    const message = useMessage()
    const loading = ref(false)
    const list = ref([
      { id: 1, userName: 'admin', permissions: ['home', 'projects', 'resource', 'datasource', 'monitor', 'security', 'system'] },
      { id: 2, userName: 'user1', permissions: ['home', 'projects'] }
    ])

    const menuOptions = [
      { label: 'Home', value: 'home' },
      { label: 'Project', value: 'projects' },
      { label: 'Resources', value: 'resource' },
      { label: 'Datasource', value: 'datasource' },
      { label: 'Monitor', value: 'monitor' },
      { label: 'Security', value: 'security' },
      { label: 'System', value: 'system' }
    ]

    const handleUpdatePermissions = (row: any, value: string[]) => {
      row.permissions = value
      message.success(`Updated permissions for ${row.userName}`)
    }

    const columns = [
      { title: 'User Name', key: 'userName' },
      {
        title: 'Menu Permissions',
        key: 'permissions',
        render: (row: any) => (
          <NCheckboxGroup
            value={row.permissions}
            onUpdateValue={(val: string[]) => handleUpdatePermissions(row, val)}
          >
            <NSpace>
              {menuOptions.map((opt) => (
                <NCheckbox value={opt.value} label={opt.label} />
              ))}
            </NSpace>
          </NCheckboxGroup>
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
      <Card title={this.t('menu.user_permission_manage')}>
        <NDataTable
          loading={this.loading}
          columns={this.columns}
          data={this.list}
        />
      </Card>
    )
  }
})

export default PermissionManage
