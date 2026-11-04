package committee.nova.mods.renderblender.client.shader;

import com.mojang.blaze3d.shaders.AbstractUniform;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import committee.nova.mods.renderblender.RenderBlenderLib;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

/**
 * 着色器 Uniform 管理与状态。
 * RegisterShadersEvent 在 MOD 总线，单独由 RBShadersSetup 处理。
 * 本类订阅 GAME 总线事件（Tick / Screen）。
 */
@EventBusSubscriber(modid = RenderBlenderLib.MOD_ID, value = Dist.CLIENT)
public class RBShaders {

    public static final float[] COSMIC_UVS = new float[40];
    public static final float[] ETERNAL_UVS = new float[40];
    public static final float[] HELL_UVS = new float[40];
    public static final float[] UNSTABLE_UVS = new float[40];

    public static boolean inventoryRender = false;
    public static int renderTime;
    public static float renderFrame;

    public static ShaderInstance COSMIC_SHADER;
    public static ShaderInstance COSMIC_ARMOR_SHADER;
    public static ShaderInstance ETERNAL_SHADER;
    public static ShaderInstance HELL_SHADER;
    public static ShaderInstance UNSTABLE_SHADER;
    public static ShaderInstance GLOW_EDGE_SHADER;

    public static AbstractUniform cosmicTime;
    public static AbstractUniform cosmicYaw;
    public static AbstractUniform cosmicPitch;
    public static AbstractUniform cosmicExternalScale;
    public static AbstractUniform cosmicOpacity;
    public static AbstractUniform cosmicUVs;

    public static AbstractUniform eternalTime;
    public static AbstractUniform eternalYaw;
    public static AbstractUniform eternalPitch;
    public static AbstractUniform eternalExternalScale;
    public static AbstractUniform eternalOpacity;
    public static AbstractUniform eternalUVs;

    public static AbstractUniform hellTime;
    public static AbstractUniform hellYaw;
    public static AbstractUniform hellPitch;
    public static AbstractUniform hellExternalScale;
    public static AbstractUniform hellOpacity;
    public static AbstractUniform hellUVs;

    public static AbstractUniform unstableTime;
    public static AbstractUniform unstableYaw;
    public static AbstractUniform unstablePitch;
    public static AbstractUniform unstableExternalScale;
    public static AbstractUniform unstableOpacity;
    public static AbstractUniform unstableUVs;

    public static AbstractUniform glowColor;
    public static AbstractUniform glowWidth;
    public static AbstractUniform glowTime;

    public static ResourceLocation rl(String path) {
        return RenderBlenderLib.rl(path);
    }

    @SubscribeEvent
    public static void clientTick(ClientTickEvent.Post event) {
        renderTime++;
    }

    @SubscribeEvent
    public static void renderTick(RenderFrameEvent.Pre event) {
        renderFrame = event.getPartialTick().getGameTimeDeltaPartialTick(false);
    }

    @SubscribeEvent
    public static void drawScreenPre(ScreenEvent.Render.Pre event) {
        inventoryRender = true;
    }

    @SubscribeEvent
    public static void drawScreenPost(ScreenEvent.Render.Post event) {
        inventoryRender = false;
    }
}