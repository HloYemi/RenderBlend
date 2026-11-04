package committee.nova.mods.renderblender.client.model.loader;

import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import committee.nova.mods.renderblender.api.client.render.buffer.AlphaOverrideVertexConsumer;
import committee.nova.mods.renderblender.api.client.util.TransformUtils;
import committee.nova.mods.renderblender.api.iface.transform.IToolTransform;
import committee.nova.mods.renderblender.client.model.loader.base.HaloSetting;
import committee.nova.mods.renderblender.client.model.loader.base.HaloUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Random;

/**
 * Name: RenderBlenderLib-forge / HaloBakedModel
 * Author: cnlimiter
 * CreateTime: 2023/9/24 22:35
 * Description:
 */

public class HaloBakedModel extends WrappedItemModel {
    private final Random random;
    private final BakedQuad haloQuad;
    private final HaloSetting setting;

    public HaloBakedModel(BakedModel wrapped, TextureAtlasSprite sprite, HaloSetting setting) {
        super(wrapped);
        this.random = new Random();
        this.haloQuad = HaloUtils.generateHaloQuad(sprite, setting.size(), setting.color());
        this.setting = setting;
    }

    @Override
    public void renderItem(ItemStack stack, ItemDisplayContext itemDisplayContext, PoseStack pPoseStack, MultiBufferSource bufferSource,
                           int packedLight, int packedOverlay) {
        if (itemDisplayContext == ItemDisplayContext.GUI && stack.getItem() instanceof SingularityItem
                && Screen.hasShiftDown()
            var itemRender= Minecraft.getInstance().getItemRenderer();
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 0.5, 0);
            pPoseStack.popPose();
        } else {
            if (stack.getItem() instanceof IToolTransform) {
                this.parentState = TransformUtils.DEFAULT_TOOL;
            } else {
                this.parentState = TransformUtils.DEFAULT_ITEM;
            }
            if (itemDisplayContext == ItemDisplayContext.GUI) {

                Minecraft.getInstance().getItemRenderer()
                        .renderQuadList(pPoseStack, bufferSource.getBuffer(ItemBlockRenderTypes.getRenderType(stack, true)), List.of(this.haloQuad), stack, packedLight, packedOverlay);
                if (this.setting.pulse()) {
                    pPoseStack.pushPose();
                    double scale = random.nextDouble() * 0.15D + 0.95D;
                    double trans = (1.0D - scale) / 2.0D;
                    pPoseStack.translate(trans, trans, 0.0D);
                    pPoseStack.scale((float) scale, (float) scale, 1.0001F);
                    this.renderWrapped(stack, pPoseStack, bufferSource, packedLight, packedOverlay, true, (e) -> new AlphaOverrideVertexConsumer(e, 0.6000000238418579D));
                    pPoseStack.popPose();
                }

            }
            this.renderWrapped(stack, pPoseStack, bufferSource, packedLight, packedOverlay, true);
        }
    }
}
