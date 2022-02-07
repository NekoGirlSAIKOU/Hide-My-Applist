package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class GetPackageInfoHook:XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param!= null){
            val packageInfo = param.result as PackageInfo
            if (EnhancedIndividualHooks.templateConfig?.isToHidePackage(
                callerName = EnhancedIndividualHooks.hookedPackageName,
                queryApplicationInfo = packageInfo.applicationInfo
            ) == true){
                param.throwable = PackageManager.NameNotFoundException()
            }
        }
    }
}