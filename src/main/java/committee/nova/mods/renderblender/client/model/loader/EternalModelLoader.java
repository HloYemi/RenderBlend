package committee.nova.mods.renderblender.client.model.loader;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import committee.nova.mods.renderblender.client.model.loader.base.BaseGeometry;
import committee.nova.mods.renderblender.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelBaker;
import net.minecraft.client.resources.model.ModelState;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;


public class EternalModelLoader extends BaseModelLoader<EternalModelLoader.EternalGeometry> {
    public static final EternalModelLoader INSTANCE = new EternalModelLoader();

    @Override
    public @NotNull EternalGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "eternal"), BlockModel.class);
        List<ResourceLocation> eternalMaskTexture = getMasks(modelContents, "eternal");
        return new EternalGeometry(baseModel, eternalMaskTexture);

    }

    public static class EternalGeometry extends BaseGeometry<EternalGeometry> {
        private final List<ResourceLocation> maskTextures;

        public EternalGeometry(final BlockModel baseModel, final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides)  {
            BakedModel finalModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            // --- [RenderBlender] 1.21.1 兼容底模生成 ---
            finalModel = finalModel == null ? null : finalModel; // 确保不是 final
 (finalModel.getQuads(null, null, net.minecraft.util.RandomSource.create()).isEmpty()) {
                net.minecraft.client.resources.model.Material layer0 = this.baseModel.getMaterial("layer0");
                if (layer0 != null) {
                    net.minecraft.client.renderer.texture.TextureAtlasSprite sprite = spriteGetter.apply(layer0);
                    java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> quads = 
                        committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel.bakeItem(java.util.List.of(sprite));
                    finalModel = new net.minecraft.client.resources.model.SimpleBakedModel(
                        quads, new java.util.HashMap<>(), true, false, true, sprite, 
                        this.baseModel.getTransforms(), 
                        net.minecraft.client.renderer.block.model.ItemOverrides.EMPTY
                    );
                }
            }
            // ----------------------------------------
            return new EternalBakeModel(finalModel, maskTextures);
        }
    }
}
