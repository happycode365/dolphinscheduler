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

import { defineComponent, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { NTabs, NTabPane } from 'naive-ui'
import { useI18n } from 'vue-i18n'
import { useTabStore } from '@/store/tab/tab'
import styles from './index.module.scss'

const Tabs = defineComponent({
  name: 'Tabs',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const { t } = useI18n()
    const tabStore = useTabStore()

    const handleTabClick = (fullPath: string) => {
      if (fullPath !== route.fullPath) {
        router.push(fullPath)
      }
    }

    const handleTabClose = (fullPath: string) => {
      const tabs = tabStore.getTabs
      const index = tabs.findIndex((tab) => tab.fullPath === fullPath)
      if (index === -1) return

      tabStore.removeTab(fullPath)

      if (fullPath === route.fullPath) {
        const nextTab = tabs[index] || tabs[index - 1]
        if (nextTab) {
          router.push(nextTab.fullPath)
        } else {
          router.push('/')
        }
      }
    }

    watch(
      () => route.fullPath,
      () => {
        if (route.name && route.meta.title) {
          tabStore.addTab({
            name: route.name as string,
            label: route.meta.title as string,
            fullPath: route.fullPath
          })
        }
      },
      { immediate: true }
    )

    return {
      tabStore,
      handleTabClick,
      handleTabClose,
      t
    }
  },
  render() {
    return (
      <div class={styles['tabs-container']}>
        <NTabs
          value={this.tabStore.getActiveTab}
          type='card'
          closable
          onUpdateValue={this.handleTabClick}
          onClose={this.handleTabClose}
        >
          {this.tabStore.getTabs.map((tab) => (
            <NTabPane key={tab.fullPath} name={tab.fullPath} tab={this.t(tab.label)} />
          ))}
        </NTabs>
      </div>
    )
  }
})

export default Tabs
