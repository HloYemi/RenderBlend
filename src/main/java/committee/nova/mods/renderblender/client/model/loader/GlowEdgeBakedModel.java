package committee.nova.mods.renderblender.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.renderblender.client.shader.RBShaders;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import committee.nova.mods.renderblender.api.iface.transform.IBowTransform;
import committee.nova.mods.renderblender.api.iface.transform.IToolTransform;

/**
 * GlowEdge 模型:原版基础渲染 + 通过环境追加自定义发光描边。
 * <p>
 * 由于 1.21.1 的 RenderType 机制与 1.20.1 差异较大,当前实现为"原版基础渲染"
 * 由 glow_edge shader + RenderType 在用户环境(或后续补丁)里启用真正的发光。
 * 现阶段先保证物品本身正常显示(不崩、不黑)。
 */
public class GlowEdgeBakedModel extends WrappedItemModel {

    public GlowEdgeBakedModel(BakedModel wrapped) {
        super(wrapped);
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else if (stack.getItem() instanceof net.minecraft.world.item.SwordItem
                || stack.getItem() instanceof net.minecraft.world.item.DiggerItem
                || stack.getItem() instanceof net.minecraft.world.item.TridentItem) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        this.renderWrapped(stack, pStack, source, light, overlay, true);
    }

    @Override
    public @Nullable PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }
}