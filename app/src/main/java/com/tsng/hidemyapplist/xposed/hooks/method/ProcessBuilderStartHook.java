package com.tsng.hidemyapplist.xposed.hooks.method;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;

import com.tsng.hidemyapplist.xposed.hooks.EnhancedIndividualHooks;

import java.lang.reflect.Field;
import java.util.List;

import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;

public class ProcessBuilderStartHook extends XC_MethodHook {
    @Override
    protected void beforeHookedMethod(MethodHookParam param) throws Throwable {
        super.beforeHookedMethod(param);
        Field field = param.thisObject.getClass().getDeclaredField("command");
        field.setAccessible(true);
        List<String> command = (List<String>) field.get(param.thisObject);
        if (command == null){
            return;
        }
        boolean flag_pm = false;
        boolean flag_list = false;
        boolean flag_packages = false;
        StringBuilder commandString = new StringBuilder();
        for (String arg:command){
            commandString.append(arg).append(" ");
            switch (arg){
                case "pm":
                    flag_pm = true;
                    break;
                case "list":
                    flag_list = true;
                    break;
                case "packages":
                    flag_packages = true;
                    break;
            }
        }
        XposedBridge.log("ProcessBuilderStartHook: Try to execute shell command: " + commandString);
        if (flag_pm && flag_list && flag_packages){
            command.clear();
            command.add("echo");
            try {
                PackageManager pm =  EnhancedIndividualHooks.getSystemContext().getPackageManager();
                List<PackageInfo> packages = pm.getInstalledPackages(0);
                for (PackageInfo pkgInfo:packages){
                    command.add("package:"+pkgInfo.packageName+"\n");
                }
            } catch (Throwable ignored){

            }
        }
    }
}