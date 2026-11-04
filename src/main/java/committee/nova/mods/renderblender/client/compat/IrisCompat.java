package committee.nova.mods.renderblender.client.compat;

import java.lang.reflect.Method;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Iris 光影兼容层。
 * <p>
 * 同时提供两条能力:
 * <ul>
 *   <li>{@link #shouldDefer(ItemDisplayContext)}:旧的"延迟渲染"兼容入口(历史保留,
 *       当前实现始终返回 false  意味着不延迟,直接渲染);</li>
 *   <li>{@link #shouldSkipCustomShaderRendering()}:Iris 光影兼容  光影激活且
 *       allowUnknownShaders=false 时,自定义 core shader 会被 Iris 跳过绘制,
 *       调用方应主动跳过自定义 shader 层(避免画成纯黑/马赛克)。</li>
 * </ul>
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
        }
        try {
            Class<?> irisClass = Class.forName("net.irisshaders.iris.Iris");
            configInstance = irisClass.getMethod("getIrisConfig").invoke(null);
            shouldAllowUnknown = configInstance.getClass().getMethod("shouldAllowUnknownShaders");
        } catch (Throwable ignored) {
        }
        IRIS_API_INSTANCE = apiInstance;
        IRIS_IS_SHADER_PACK_IN_USE = isShaderPackInUse;
        IRIS_CONFIG_INSTANCE = configInstance;
        IRIS_SHOULD_ALLOW_UNKNOWN_SHADERS = shouldAllowUnknown;
    }

    private IrisCompat() {
    }

    /**
     * 是否需要延迟渲染(历史兼容入口)。
     * <p>
     * 旧版本用此入口判断"是否应该把 cosmic 延迟到 Iris 之后渲染"。当前实现直接
     * 返回 false = 不延迟:即由 BakeModel 在 normal 阶段直接渲染(与 1.20.1 语义)。
     * 保留此方法避免已有调用点编译失败。
     *
     * @param transformType 显示上下文(此实现忽略)
     * @return false(不延迟)
     */
    public static boolean shouldDefer(ItemDisplayContext transformType) {
        return false;
    }

    /**
     * 光影是否激活且不允许未知 shader  自定义 shader 的绘制会被 Iris 跳过。
     * 此时调用方应主动跳过自定义 shader 渲染(宇宙/永久/地狱/不稳定层)。
     * <p>
     * 反射检测有真实开销,按 500ms 缓存结果。未装 Iris 或检测失败一律返回 false
     * (保持原有渲染,不主动跳过)。
     *
     * @return true = 应跳过自定义 shader 渲染
     */
    public static boolean shouldSkipCustomShaderRendering() {
        if (IRIS_IS_SHADER_PACK_IN_USE == null || IRIS_SHOULD_ALLOW_UNKNOWN_SHADERS == null) {
            return false;
        }
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
            return false;
        }
    }
}