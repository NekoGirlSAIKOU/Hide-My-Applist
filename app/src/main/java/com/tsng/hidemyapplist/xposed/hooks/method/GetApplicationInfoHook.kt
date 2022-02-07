package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class GetApplicationInfoHook:XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param!= null){
            val applicationInfo = param.result as ApplicationInfo
            if (EnhancedIndividualHooks.templateConfig?.isToHidePackage(
                    callerName = EnhancedIndividualHooks.hookedPackageName,
                    queryApplicationInfo = applicationInfo
                ) == true){
                param.throwable = PackageManager.NameNotFoundException()
            }
        }
    }
}