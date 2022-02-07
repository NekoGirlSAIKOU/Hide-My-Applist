#include <jni.h>
#include <fcntl.h>
#include <unistd.h>
#include <sys/stat.h>
#include <android/log.h>
#include <queue>
#include <regex>
#include <cstdio>
#include <sstream>
#include <algorithm>
#include "dobby.h"
#include "json.hpp"

using std::string;

constexpr char APPNAME[] = "com.tsng.hidemyapplist";

struct Config {
    bool WhiteList = false;
    bool EnableAllHooks = false;
    bool ExcludeSystemApps = false;
    std::vector<string> HideApps;
} config;

template<>
struct jsonxx::json_bind<Config> {
    static void from_json(const json &j, Config &v) {
        jsonxx::from_json(j["EnableAllHooks"], v.EnableAllHooks);
        jsonxx::from_json(j["HideApps"], v.HideApps);
        jsonxx::from_json(j["ExcludeSystemApps"], v.ExcludeSystemApps);
        jsonxx::from_json(j["WhiteList"], v.WhiteList);
    }
};

const char *callerName;
std::queue<string> messageQueue;

inline void ld(const string &s) {
    messageQueue.push("DEBUG");
    messageQueue.push(s);
    __android_log_print(ANDROID_LOG_DEBUG, "[HMA Native]", "[DEBUG] %s", s.c_str());
}

inline void li(const string &s) {
    messageQueue.push("INFO");
    messageQueue.push(s);
    __android_log_print(ANDROID_LOG_INFO, "[HMA Native]", "[INFO] %s", s.c_str());
}

inline void le(const string &s) {
    messageQueue.push("ERROR");
    messageQueue.push(s);
    __android_log_print(ANDROID_LOG_ERROR, "[HMA Native]", "[ERROR] %s", s.c_str());
}

bool isUseHook(const string &hookMethod) {
    return true;
}

bool isHideFile(const char *path) {
    if (callerName == nullptr || path == nullptr) return false;
    if (strstr(path, callerName) != nullptr) return false;
    const auto &tpl = config;
    if (std::regex_search(path, std::regex("/storage/emulated/(.*)/Android/")) ||
        strstr(path, "/sdcard/Android/") != nullptr ||
        strstr(path, "/data/data/") != nullptr ||
        strstr(path, "/data/user/") != nullptr) { // 如果包含了敏感路径
        for (const auto &pkg : tpl.HideApps)
            if (strstr(path, pkg.c_str()) != nullptr) // 如果路径包含应用列表中的APP
                return !tpl.WhiteList; // 包含白名单，不隐藏；包含黑名单，隐藏
        return tpl.WhiteList; // 路径不含列表中应用，白名单隐藏，黑名单不隐藏
    }
    // 这里仍然有个问题没解决：native hooks目前无法判断白名单模式下排除系统应用，不过一般不会要读取系统应用的目录，所以暂时先不管，之后修
    return false;
}

int (*orig_access)(const char *path, int mode);
int fake_access(const char *path, int mode) {
    if (isUseHook("File detections") && isHideFile(path)) {
        std::stringstream message;
        message << "@Hide nativeAccess caller: " << callerName << " param: " << path;
        li(message.str());
        return -1;
    }
    return orig_access(path, mode);
}

int (*orig_stat)(const char *path, struct stat *buf);
int fake_stat(const char *path, struct stat *buf) {
    if (isUseHook("File detections") && isHideFile(path)) {
        std::stringstream message;
        message << "@Hide nativeStat caller: " << callerName << " param: " << path;
        li(message.str());
        return -1;
    }
    return orig_stat(path, buf);
}

int (*orig_open)(const char *path, int flags, ...);
int fake_open(const char *path, int flags, ...) {
    if (isUseHook("File detections") && isHideFile(path)) {
        // This may cause crash. Seems it has been fixed.
        std::stringstream message;
        message << "@Hide nativeOpen caller: " << callerName << " param: " << path;
        li(message.str());
        return -1;
    }
    mode_t mode = 0;
    if (flags & O_CREAT) {
        va_list args;
        va_start(args, flags);
        mode = (mode_t) va_arg(args, int);
        va_end(args);
    }
    return orig_open(path, flags, mode);
}

extern "C" JNIEXPORT void JNICALL
Java_com_tsng_hidemyapplist_xposed_hooks_IndividualHooks_initNative(JNIEnv *env, jobject, jstring j_pkgName) {
    callerName = env->GetStringUTFChars(j_pkgName, nullptr);
    DobbyHook((void *) access, (void *) fake_access, (void **) &orig_access);
    DobbyHook((void *) stat, (void *) fake_stat, (void **) &orig_stat);
    int (*p_orig_open)(const char *, int) = __open_2;
    DobbyHook((void *) p_orig_open, (void *) fake_open, (void **) &orig_open);
    DobbyHook((void *) DobbySymbolResolver(nullptr, "open"), (void *) fake_open, (void **) &orig_open);
}

extern "C" JNIEXPORT jobjectArray JNICALL
Java_com_tsng_hidemyapplist_xposed_hooks_IndividualHooks_nativeBridge(JNIEnv *env, jobject, jstring j_json) {
    jsonxx::from_json(jsonxx::json::parse(env->GetStringUTFChars(j_json, nullptr)), config);
    int length = messageQueue.size();
    jobjectArray ret = env->NewObjectArray(length, env->FindClass("java/lang/String"), nullptr);
    for (int i = 0; i < length; i++) {
        env->SetObjectArrayElement(ret, i, env->NewStringUTF(messageQueue.front().c_str()));
        messageQueue.pop();
    }
    return ret;
}