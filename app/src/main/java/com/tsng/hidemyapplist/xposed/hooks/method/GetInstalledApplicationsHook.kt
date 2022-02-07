package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.ApplicationInfo
import com.tsng.hidemyapplist.utils.isSystemApp
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class GetInstalledApplicationsHook: XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param != null){
            val result = param.result as MutableList<ApplicationInfo>
            param.result = result.filter {
                EnhancedIndividualHooks.templateConfig?.isToHidePackage(
                    callerName = EnhancedIndividualHooks.hookedPackageName,
                    queryApplicationInfo = it
                ) != true
            }.toMutableList()
        }
    }
}