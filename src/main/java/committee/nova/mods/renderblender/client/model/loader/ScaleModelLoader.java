package committee.nova.mods.renderblender.client.model.loader;
import com.google.gson.*;
import committee.nova.mods.renderblender.client.model.loader.base.BaseGeometry;
import committee.nova.mods.renderblender.client.model.loader.base.BaseModelLoader;
import net.minecraft.client.renderer.block.model.BlockModel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.*;
import net.minecraft.util.GsonHelper;
import net.neoforged.neoforge.client.model.geometry.IGeometryBakingContext;
import org.jetbrains.annotations.NotNull;
import java.util.function.Function;

public class ScaleModelLoader extends BaseModelLoader<ScaleModelLoader.ScaleGeometry> {
    public static final ScaleModelLoader INSTANCE = new ScaleModelLoader();
    @Override public @NotNull ScaleGeometry read(@NotNull JsonObject json, JsonDeserializationContext ctx) throws JsonParseException {
        JsonObject scale = json.getAsJsonObject("scale");
        float gui = GsonHelper.getAsFloat(scale, "gui_scale", 1.0f);
        float hand = GsonHelper.getAsFloat(scale, "hand_scale", 1.0f);
        BlockModel base = ctx.deserialize(clear(json, "scale"), BlockModel.class);
        return new ScaleGeometry(base, gui, hand);
    }
    public static class ScaleGeometry extends BaseGeometry<ScaleGeometry> {
        private final float gui, hand;
        public ScaleGeometry(BlockModel base, float gui, float hand) { super(base); this.gui = gui; this.hand = hand; }
        @Override public @NotNull BakedModel bake(@NotNull IGeometryBakingContext ctx, @NotNull ModelBaker baker, @NotNull Function<Material, TextureAtlasSprite> spr, @NotNull ModelState st, @NotNull ItemOverrides ov) {
            return new ScaleBakedModel(this.baseModel.bake(baker, this.baseModel, spr, st, true), gui, hand);
        }
    }
}