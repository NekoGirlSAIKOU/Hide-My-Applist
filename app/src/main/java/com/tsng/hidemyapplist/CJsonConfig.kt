package com.tsng.hidemyapplist

import com.google.gson.Gson

class CJsonConfig(
    var WhiteList: Boolean = false,
    var EnableAllHooks: Boolean = false,
    var ExcludeSystemApps: Boolean = false,
    var HideApps: Set<String> = setOf<String>()
) {
    override fun toString(): String {
        return Gson().toJson(this)
    }
}