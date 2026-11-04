package committee.nova.mods.renderblender.client.compat;

import java.lang.reflect.Method;

/**
 * Iris 光影兼容层(借鉴 Enchanted Outlines 的 needVanillaShaderFallback 设计)。
 * <p>
 * Iris 接管世界渲染后,自定义 core shader(如 renderblender:cosmic/eternal/hell/unstable)
 * 的绘制由 Iris 的 allowUnknownShaders 配置决定:
 * <ul>
 *   <li>未开光影(未装 Iris 或未开光影包) 自定义 shader 原样工作,效果最佳;</li>
 *   <li>光影激活 + allowUnknownShaders=true  自定义 shader 可渲染,效果保留;</li>
 *   <li>光影激活 + allowUnknownShaders=false(Iris 默认) 自定义 shader 的绘制
 *       被 Iris 静默跳过(宇宙层不可见,可能画成纯黑马赛克)。</li>
 * </ul>
 * 第三种情况下,本库的 *BakeModel 应在渲染原版层之后主动跳过自定义 shader 层,
 * 避免生成错误渲染(纯黑/透明)而非"看不到"。用户开光影后看不到 cosmic 特效是
 * 已知限制,比"看到纯黑马赛克"体验好。
 * <p>
 * 反射检测有真实开销(两次 invoke),按 500ms 缓存结果。光影包切换是低频操作,
 * 500ms 内切换最多延迟半秒生效,可接受。
 */
public final class IrisCompat {

    private static final Object IRIS_API_INSTANCE;
    private static final Method IRIS_IS_SHADER_PACK_IN_USE;
    private static final Object IRIS_CONFIG_INSTANCE;
    private static final Method IRIS_SHOULD_ALLOW_UNKNOWN_SHADERS;

    private static long lastCheckMs;
    private static boolean cachedResult;

    static {
        Object apiInstance = null;
        Method isShaderPackInUse = null;
        Object configInstance = null;
        Method shouldAllowUnknown = null;
        try {
            Class<?> apiClass = Class.forName("net.irisshaders.iris.api.v0.IrisApi");
            apiInstance = apiClass.getMethod("getInstance").invoke(null);
            isShaderPackInUse = apiInstance.getClass().getMethod("isShaderPackInUse");
        } catch (Throwable ignored) {
            // 未安装 Iris / API 不兼容
        }
        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            configInstance = irisClass.getMethod("getIrisConfig").invoke(null);
            shouldAllowUnknown = configInstance.getClass().getMethod("shouldAllowUnknownShaders");
        } catch (Throwable ignored) {
            // 未安装 Iris / 配置方法不存在
        }
        IRIS_API_INSTANCE = apiInstance;
        IRIS_IS_SHADER_PACK_IN_USE = isShaderPackInUse;
        IRIS_CONFIG_INSTANCE = configInstance;
        IRIS_SHOULD_ALLOW_UNKNOWN_SHADERS = shouldAllowUnknown;
    }

    private IrisCompat() {
    }

    /**
     * 光影是否激活且不允许未知 shader  自定义 shader 的绘制会被 Iris 跳过。
     * 此时调用方应主动跳过自定义 shader 渲染(宇宙/永久/地狱/不稳定层)。
     *
     * @return true = 应跳过自定义 shader 渲染
     */
    public static boolean shouldSkipCustomShaderRendering() {
        long now = System.currentTimeMillis();
        if (now - lastCheckMs >= 500L) {
            cachedResult = check();
            lastCheckMs = now;
        }
        return cachedResult;
    }

    private static boolean check() {
        try {
            boolean active = (Boolean) IRIS_IS_SHADER_PACK_IN_USE.invoke(IRIS_API_INSTANCE);
            if (!active) {
                return false;
            }
            boolean allow = (Boolean) IRIS_SHOULD_ALLOW_UNKNOWN_SHADERS.invoke(IRIS_CONFIG_INSTANCE);
            return !allow;
        } catch (Throwable ignored) {
            // 反射失败(Iris 未安装/版本不兼容):不跳过,保持原有渲染
            return false;
        }
    }
}