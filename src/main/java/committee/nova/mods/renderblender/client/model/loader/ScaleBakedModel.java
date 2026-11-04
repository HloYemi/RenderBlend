package committee.nova.mods.renderblender.client.model.loader;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import org.jetbrains.annotations.NotNull;

public class ScaleBakedModel extends BakedModelWrapper<BakedModel> {
    private final float guiScale, handScale;
    public ScaleBakedModel(BakedModel originalModel, float guiScale, float handScale) {
        super(originalModel); this.guiScale = guiScale; this.handScale = handScale;
    }
    @Override public @NotNull BakedModel applyTransform(@NotNull ItemDisplayContext transformType, @NotNull PoseStack poseStack, boolean applyLeftHandTransform) {
        switch (transformType) {
            case GUI -> { if (guiScale != 1.0f) poseStack.scale(guiScale, guiScale, guiScale); }
            case FIRST_PERSON_LEFT_HAND, FIRST_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND, THIRD_PERSON_RIGHT_HAND -> { if (handScale != 1.0f) poseStack.scale(handScale, handScale, handScale); }
        }
        return super.applyTransform(transformType, poseStack, applyLeftHandTransform);
    }
}