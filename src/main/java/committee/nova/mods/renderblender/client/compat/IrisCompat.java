package committee.nova.mods.renderblender.client.compat;

import net.minecraft.world.item.ItemDisplayContext;

/**
 * Iris 兼容层简化版。
 * 不依赖 Iris API，始终返回 false（不使用延迟渲染）。
 * 未来如需兼容 Iris，可在此处改用反射判断。
 */
public class IrisCompat {
    public static boolean shouldDefer(ItemDisplayContext transformType) {
        return false;
    }
}