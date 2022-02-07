package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.PackageManager
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class CheckPermissionHook:XC_MethodHook() {
    override fun beforeHookedMethod(param: MethodHookParam?) {
        super.beforeHookedMethod(param)
        if (param != null){
            val packageName = param.args[1] as String
            if (EnhancedIndividualHooks.isToHidePackage(packageName)){
                // Hide it
                param.result = PackageManager.PERMISSION_DENIED
            }
        }
    }
}