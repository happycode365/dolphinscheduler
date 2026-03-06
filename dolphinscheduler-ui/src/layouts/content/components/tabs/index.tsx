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
import { useTabStore } from '@/store/tab'

const Tabs = defineComponent({
  name: 'Tabs',
  setup() {
    const route = useRoute()
    const router = useRouter()
    const tabStore = useTabStore()

    const handleTabClick = (path: string) => {
      router.push({ path })
    }

    const handleTabClose = (path: string) => {
      tabStore.removeTab(path)
      if (tabStore.getActiveTab) {
        router.push({ path: tabStore.getActiveTab })
      } else {
        router.push({ path: '/home' })
      }
    }

    watch(
      () => tabStore.getActiveTab,
      (path) => {
        if (path && path !== route.path) {
          router.push({ path })
        }
      }
    )

    return {
      tabStore,
      handleTabClick,
      handleTabClose
    }
  },
  render() {
    return (
      <div style='padding: 0 20px; background: var(--n-color);'>
        <NTabs
          value={this.tabStore.getActiveTab}
          type='card'
          closable
          onUpdateValue={this.handleTabClick}
          onClose={this.handleTabClose}
        >
          {this.tabStore.getTabs.map((tab) => (
            <NTabPane
              key={tab.path}
              name={tab.path}
              tab={tab.label}
            />
          ))}
        </NTabs>
      </div>
    )
  }
})

export default Tabs
