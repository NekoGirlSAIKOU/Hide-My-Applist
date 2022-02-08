package com.tsng.hidemyapplist.xposed.hooks

import android.app.Application
import android.content.Context
import android.util.Log
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.tsng.hidemyapplist.BuildConfig
import com.tsng.hidemyapplist.CJsonConfig
import com.tsng.hidemyapplist.JsonConfig
import com.tsng.hidemyapplist.utils.isSystemApp
import com.tsng.hidemyapplist.xposed.XposedEntry.Companion.modulePath
import com.tsng.hidemyapplist.xposed.XposedUtils
import com.tsng.hidemyapplist.xposed.XposedUtils.L
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam
import java.io.File
import kotlin.concurrent.thread

class IndividualHooks : IXposedHookLoadPackage {
    companion object{
        private const val TAG = "IndividualHooks"
    }

    override fun handleLoadPackage(lpp: LoadPackageParam) {
        if (lpp.appInfo == null || lpp.appInfo.isSystemApp()) return
        XposedHelpers.findAndHookMethod(Application::class.java, "attach", Context::class.java, object : XC_MethodHook() {
            override fun afterHookedMethod(param: MethodHookParam) {
                val context = param.args[0] as Context
                var loadedNativeLib = false
                if (BuildConfig.BUILD_TYPE != "non_native") try {
                    System.load(modulePath.substring(0, modulePath.lastIndexOf('/'))
                            + if (android.os.Process.is64Bit()) "/lib/arm64/libhma_native_hooks.so" else "/lib/arm/libhma_native_hooks.so")
                    loadedNativeLib = true
                } catch (e: Throwable) {
                    L.e("Load native_hooks library failed | caller: ${lpp.packageName}\n${e.stackTraceToString()}", context = context)
                }
                if (loadedNativeLib) nativeHook(context, lpp.packageName)
                else fileHook(context, lpp.packageName)
            }
        })
    }

    fun fileHook(context: Context, pkgName: String) {
        XposedHelpers.findAndHookConstructor(File::class.java, String::class.java, object : XC_MethodHook() {
            override fun beforeHookedMethod(param: MethodHookParam) {
                if (EnhancedIndividualHooks.templateConfig?.applyHooks?.fileDetections == true){
                    if (EnhancedIndividualHooks.templateConfig?.isToHideFile(pkgName,param.args[0] as String) == true){
                        L.i("@Hide javaFile caller: $pkgName param: ${param.args[0]}", context = context)
                        param.args[0] = "fuck/there/is/no/file"
                    }
                }
            }
        })
    }

    fun nativeHook(context: Context, pkgName: String) {
        initNative(pkgName)
        thread {
            var json = getJson()?.toString()
            while (true) {
                if (json == null){
                    json = getJson()?.toString()
                }
                if (json != null) {
                    var last = "/"
                    val messages = nativeBridge(json)
                    val iterator = messages.iterator()
                    while (iterator.hasNext()) {
                        when (val str = iterator.next()) {
                            "DEBUG" -> last = "d"
                            "INFO" -> last = "i"
                            "ERROR" -> last = "e"
                            else -> when (last) {
                                "d" -> Log.d(TAG,str)
                                "i" -> Log.i(TAG,str)
                                "e" -> Log.e(TAG,str)
                            }
                        }
                    }
                }
                Thread.sleep(1000)
            }
        }
    }

    private external fun initNative(pkgName: String)
    private external fun nativeBridge(json: String) : Array<String>

    fun getJson(): CJsonConfig? {
        return EnhancedIndividualHooks.templateConfig?.let {
            CJsonConfig(
                WhiteList =  it.whiteList,
                EnableAllHooks = it.enableAllHooks,
                ExcludeSystemApps = it.excludeSystemApps,
                HideApps = HashSet(it.hideApps).apply {
                    if (it.whiteList){
                        add(EnhancedIndividualHooks.hookedPackageName)  // Don't hide self.
                        add("com.mali.testjava")    // This may cause crash if hides its path.
                    } else {
                        remove(EnhancedIndividualHooks.hookedPackageName)  // Don't hide self.
                    }
                }
            )
        }
    }
}