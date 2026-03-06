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

interface Tab {
  path: string
  label: string
}

interface TabState {
  tabs: Tab[]
  activeTab: string
}

export const useTabStore = defineStore({
  id: 'tab',
  state: (): TabState => ({
    tabs: [],
    activeTab: ''
  }),
  persist: true,
  getters: {
    getTabs(): Tab[] {
      return this.tabs
    },
    getActiveTab(): string {
      return this.activeTab
    }
  },
  actions: {
    addTab(tab: Tab): void {
      if (!this.tabs.find((t) => t.path === tab.path)) {
        this.tabs.push(tab)
      }
      this.activeTab = tab.path
    },
    removeTab(path: string): void {
      const index = this.tabs.findIndex((t) => t.path === path)
      if (index === -1) return

      this.tabs.splice(index, 1)
      if (this.activeTab === path) {
        const nextTab = this.tabs[index] || this.tabs[index - 1]
        this.activeTab = nextTab ? nextTab.path : ''
      }
    },
    setActiveTab(path: string): void {
      this.activeTab = path
    },
    resetTabs(): void {
      this.tabs = []
      this.activeTab = ''
    }
  }
})
