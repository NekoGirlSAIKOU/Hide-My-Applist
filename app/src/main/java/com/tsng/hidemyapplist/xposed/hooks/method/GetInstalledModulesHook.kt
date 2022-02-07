package com.tsng.hidemyapplist.xposed.hooks.method

import android.content.pm.ApplicationInfo
import android.content.pm.ModuleInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks
import de.robv.android.xposed.XC_MethodHook

@RequiresApi(Build.VERSION_CODES.Q)
class GetInstalledModulesHook:XC_MethodHook() {
    override fun afterHookedMethod(param: MethodHookParam?) {
        super.afterHookedMethod(param)
        if (param != null){
            val result = param.result as MutableList<ModuleInfo>
            param.result = result.filter {
                !EnhancedIndividualHooks.isToHidePackage(it.packageName)
            }.toMutableList()
        }
    }
}