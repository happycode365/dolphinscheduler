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

import { defineStore } from 'pinia'

export interface Tab {
  name: string
  path: string
  label: string
}

export const useTabStore = defineStore('tab', {
  state: () => ({
    tabs: [] as Tab[],
    activeTab: ''
  }),
  actions: {
    addTab(tab: Tab) {
      if (this.tabs.every((t) => t.path !== tab.path)) {
        this.tabs.push(tab)
      }
      this.activeTab = tab.path
    },
    removeTab(path: string) {
      const index = this.tabs.findIndex((t) => t.path === path)
      if (index !== -1) {
        this.tabs.splice(index, 1)
        if (this.activeTab === path) {
          this.activeTab =
            this.tabs.length > 0 ? this.tabs[this.tabs.length - 1].path : ''
        }
      }
    },
    setActiveTab(path: string) {
      this.activeTab = path
    }
  },
  persist: true
})
