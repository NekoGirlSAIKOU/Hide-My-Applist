package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.PackageInfo
import com.tsng.hidemyapplist.utils.isSystemApp
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

open class GetInstalledPackagesHook : XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param != null) {
            val result = param.result as MutableList<PackageInfo>
            param.result = result.filter {
                EnhancedIndividualHooks.templateConfig?.isToHidePackage(
                    callerName = EnhancedIndividualHooks.hookedPackageName,
                    queryApplicationInfo = it.applicationInfo
                ) != true
            }.toMutableList()
        }
    }
}