package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

open class GetPackageUidHook:XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if(param != null){
            val packageName = param.args[0] as String
            if (EnhancedIndividualHooks.isToHidePackage(packageName)){
                param.throwable = PackageManager.NameNotFoundException()
            }
        }
    }
}