package committee.nova.mods.renderblender.client.model.loader;
import com.google.gson.*;
import committee.nova.mods.renderblender.client.model.loader.base.BaseGeometry;
import committee.nova.mods.renderblender.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public class GlowEdgeModelLoader extends BaseModelLoader<GlowEdgeModelLoader.GlowEdgeGeometry> {
    public static final GlowEdgeModelLoader INSTANCE = new GlowEdgeModelLoader();
    @Override public @NotNull GlowEdgeGeometry read(@NotNull JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        BlockModel base = ctx.deserialize(clear(json, "glow_edge"), BlockModel.class);
        return new GlowEdgeGeometry(base);
    }
    public static class GlowEdgeGeometry extends BaseGeometry<GlowEdgeGeometry> {
        public GlowEdgeGeometry(BlockModel base) { super(base); }
        @Override public @NotNull BakedModel bake(@NotNull IGeometryBakingContext ctx, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spr, @NotNull ModelState st, @NotNull ItemOverrides ov) {
            return new GlowEdgeBakedModel(this.baseModel.bake(baker, this.baseModel, spr, st, true));
        }
    }
}