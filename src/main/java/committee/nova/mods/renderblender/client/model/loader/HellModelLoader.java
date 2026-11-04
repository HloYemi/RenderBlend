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

public class HellModelLoader extends BaseModelLoader<HellModelLoader.HellGeometry> {
    public static final HellModelLoader INSTANCE = new HellModelLoader();

    @Override
    public @NotNull HellGeometry read(@NotNull JsonObject modelContents, JsonDeserializationContext deserializationContext) throws JsonParseException {
        BlockModel baseModel = deserializationContext.deserialize(clear(modelContents, "hell"), BlockModel.class);
        List<ResourceLocation> hellMaskTexture = getMasks(modelContents, "hell");

        return new HellGeometry(baseModel, hellMaskTexture);
    }

    public static class HellGeometry extends BaseGeometry<HellGeometry> {
        private final List<ResourceLocation> maskTextures;

        public HellGeometry(final BlockModel baseModel, final List<ResourceLocation> maskTextures) {
            super(baseModel);
            this.maskTextures = maskTextures;
        }

        @Override
        public @NotNull BakedModel bake(@NotNull IGeometryBakingContext context, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spriteGetter, @NotNull ModelState modelState, @NotNull ItemOverrides overrides)  {
            BakedModel finalModel = this.baseModel.bake(baker, this.baseModel, spriteGetter, modelState, true);
            // --- [RenderBlender] 1.21.1 兼容底模补丁 ---
            BakedModel finalModel = finalModel;
            {varName = null; // 解除原变量引用
 (finalModel.getQuads(null, null, net.minecraft.util.RandomSource.create()).isEmpty()) {
                java.util.List<net.minecraft.client.renderer.texture.TextureAtlasSprite> sprites = new java.util.ArrayList<>();
                net.minecraft.client.resources.model.Material layer0 = this.baseModel.getMaterial("layer0");
                if (layer0 != null) sprites.add(spriteGetter.apply(layer0));
                net.minecraft.client.resources.model.Material particle = this.baseModel.getMaterial("particle");
                if (particle != null && particle != layer0) sprites.add(spriteGetter.apply(particle));
                
                if (!sprites.isEmpty()) {
                    java.util.List<net.minecraft.client.renderer.block.model.BakedQuad> quads = 
                        committee.nova.mods.renderblender.api.client.model.bakedmodels.WrappedItemModel.bakeItem(sprites);
                    finalModel = new net.minecraft.client.resources.model.SimpleBakedModel(
                        quads, new java.util.HashMap<>(), true, false, true, sprites.get(0), 
                        net.minecraft.client.renderer.block.model.ItemTransforms.NO_TRANSFORMS, 
                        net.minecraft.client.renderer.block.model.ItemOverrides.EMPTY
                    );
                }
            }
            // ----------------------------------------
            return new HellBakeModel(finalModel, maskTextures);
        }
    }
}
