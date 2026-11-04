package committee.nova.mods.renderblender.client.model.loader;
import com.mojang.blaze3d.vertex.PoseStack;
import committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public class GlowEdgeBakedModel extends WrappedItemModel {
    public GlowEdgeBakedModel(BakedModel wrapped) { super(wrapped); }
    @Override public void renderItem(ItemStack stack, ItemDisplayContext ctx, PoseStack pStack, MultiBufferSource source, int light, int overlay) {
        this.renderWrapped(stack, pStack, source, light, overlay, true);
    }
}