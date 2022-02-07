package com.tsng.hidemyapplist.xposed.hooks.method

import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

class GetApplicationEnabledSettingHook:XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param != null){
            val packageName = param.args[0] as String
            if (EnhancedIndividualHooks.isToHidePackage(packageName)){
                // Hide it
                param.throwable = IllegalArgumentException()
            }
        }
    }
}