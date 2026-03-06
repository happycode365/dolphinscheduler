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
import { NTabs, NTab } from 'naive-ui'
import { useRoute, useRouter } from 'vue-router'
import { useTabStore } from '@/store/tab'

const Tabs = defineComponent({
  name: 'Tabs',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const tabStore = useTabStore()

    const handleTabChange = (path: string) => {
      tabStore.setActiveTab(path)
      router.push({ path })
    }

    const handleTabClose = (path: string) => {
      tabStore.removeTab(path)
      if (tabStore.activeTab) {
        router.push({ path: tabStore.activeTab })
      } else {
        router.push({ path: '/home' })
      }
    }

    watch(
      () => route.fullPath,
      () => {
        if (route.path !== '/login') {
          tabStore.addTab({
            name: route.name as string,
            path: route.fullPath,
            label: (route.meta.title as string) || (route.name as string)
          })
        }
      },
      { immediate: true }
    )

    return { tabStore, handleTabChange, handleTabClose }
  },
  render() {
    return (
      <NTabs
        value={this.tabStore.activeTab}
        type='card'
        closable
        onUpdateValue={this.handleTabChange}
        onClose={this.handleTabClose}
      >
        {this.tabStore.tabs.map((tab) => (
          <NTab name={tab.path} tab={tab.label} key={tab.path} />
        ))}
      </NTabs>
    )
  }
})

export default Tabs
