package committee.nova.mods.renderblender.client.model.loader;


import committee.nova.mods.renderblender.client.model.loader.base.HaloUtils;import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.renderblender.api.client.model.CachedFormat;
import committee.nova.mods.renderblender.api.client.model.PerspectiveModelState;
import committee.nova.mods.renderblender.api.client.model.Quad;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.renderblender.api.client.render.buffer.AlphaOverrideVertexConsumer;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import committee.nova.mods.renderblender.api.client.util.color.ColorARGB;
import committee.nova.mods.renderblender.api.iface.transform.IBowTransform;
import committee.nova.mods.renderblender.api.iface.transform.IToolTransform;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

public class HaloBakedModel extends WrappedItemModel {
    private final Random random;
    private final BakedQuad haloQuad;
    private final boolean pulse;

    public HaloBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, int color, int size, boolean pulse) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = HaloUtils.generateHaloQuad(sprite, size, color);
        this.pulse = pulse;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext transformType, PoseStack pStack, MultiBufferSource source, int packedLight, int packedOverlay) {
        if (stack.getItem() instanceof IToolTransform) {
            this.parentState = TransformUtils.DEFAULT_TOOL;
        } else if (stack.getItem() instanceof IBowTransform) {
            this.parentState = TransformUtils.DEFAULT_BOW;
        } else {
            this.parentState = TransformUtils.DEFAULT_ITEM;
        }

        if (transformType == ItemDisplayContext.GUI) {
            Minecraft.getInstance().getItemRenderer()
                    .renderQuadList(pStack, source.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), List.of(this.haloQuad), stack, packedLight, packedOverlay);
            if (this.pulse) {
                pStack.pushPose();
                double scale = random.nextDouble() * 0.15D + 0.95D;
                double trans = (1.0D - scale) / 2.0D;
                pStack.translate(trans, trans, 0.0D);
                pStack.scale((float) scale, (float) scale, 1.0001F);
                this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                pStack.popPose();
            }
        }
        this.renderWrapped(stack, pStack, source, packedLight, packedOverlay, true);
    }

    @Override
    public PerspectiveModelState getModelState() {
        return (PerspectiveModelState) this.parentState;
    }
}