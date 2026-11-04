package committee.nova.mods.renderblender.client.shader;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import committee.nova.mods.renderblender.RenderBlenderLib;
import net.minecraft.client.renderer.ShaderInstance;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

/**
 * 着色器注册（MOD 总线）。
 * 每个 shader 独立 try-catch：单个失败不影响其它，也不会崩客户端。
 */
@EventBusSubscriber(modid = RenderBlenderLib.MOD_ID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class RBShadersSetup {

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) {
        // COSMIC (BLOCK)
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("cosmic"), DefaultVertexFormat.BLOCK),
                    shader -> {
                        RBShaders.COSMIC_SHADER = shader;
                        RBShaders.cosmicTime = shader.safeGetUniform("time");
                        RBShaders.cosmicYaw = shader.safeGetUniform("yaw");
                        RBShaders.cosmicPitch = shader.safeGetUniform("pitch");
                        RBShaders.cosmicExternalScale = shader.safeGetUniform("externalScale");
                        RBShaders.cosmicOpacity = shader.safeGetUniform("opacity");
                        RBShaders.cosmicUVs = shader.safeGetUniform("cosmicuvs");
                    });
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.warn("[RenderBlender] cosmic(BLOCK) shader 注册失败: {}", e.getMessage());
        }

        // COSMIC (NEW_ENTITY)  armor 用
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("cosmic"), DefaultVertexFormat.NEW_ENTITY),
                    shader -> RBShaders.COSMIC_ARMOR_SHADER = shader);
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.warn("[RenderBlender] cosmic(NEW_ENTITY) shader 注册失败: {}", e.getMessage());
        }

        // HELL
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("hell"), DefaultVertexFormat.BLOCK),
                    shader -> {
                        RBShaders.HELL_SHADER = shader;
                        RBShaders.hellTime = shader.safeGetUniform("time");
                        RBShaders.hellYaw = shader.safeGetUniform("yaw");
                        RBShaders.hellPitch = shader.safeGetUniform("pitch");
                        RBShaders.hellExternalScale = shader.safeGetUniform("externalScale");
                        RBShaders.hellOpacity = shader.safeGetUniform("opacity");
                        RBShaders.hellUVs = shader.safeGetUniform("cosmicuvs");
                    });
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.warn("[RenderBlender] hell shader 注册失败: {}", e.getMessage());
        }

        // ETERNAL
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("eternal"), DefaultVertexFormat.BLOCK),
                    shader -> {
                        RBShaders.ETERNAL_SHADER = shader;
                        RBShaders.eternalTime = shader.safeGetUniform("time");
                        RBShaders.eternalYaw = shader.safeGetUniform("yaw");
                        RBShaders.eternalPitch = shader.safeGetUniform("pitch");
                        RBShaders.eternalExternalScale = shader.safeGetUniform("externalScale");
                        RBShaders.eternalOpacity = shader.safeGetUniform("opacity");
                        RBShaders.eternalUVs = shader.safeGetUniform("cosmicuvs");
                    });
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.warn("[RenderBlender] eternal shader 注册失败: {}", e.getMessage());
        }

        // UNSTABLE
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("unstable"), DefaultVertexFormat.BLOCK),
                    shader -> {
                        RBShaders.UNSTABLE_SHADER = shader;
                        RBShaders.unstableTime = shader.safeGetUniform("time");
                        RBShaders.unstableYaw = shader.safeGetUniform("yaw");
                        RBShaders.unstablePitch = shader.safeGetUniform("pitch");
                        RBShaders.unstableExternalScale = shader.safeGetUniform("externalScale");
                        RBShaders.unstableOpacity = shader.safeGetUniform("opacity");
                        RBShaders.unstableUVs = shader.safeGetUniform("cosmicuvs");
                    });
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.warn("[RenderBlender] unstable shader 注册失败: {}", e.getMessage());
        }

        // GLOW_EDGE  Re:Avaritia 1.21.1 未提供此 shader，通常失败，会被 catch 吞掉
        try {
            event.registerShader(
                    new ShaderInstance(event.getResourceProvider(), RBShaders.rl("glow_edge"), DefaultVertexFormat.NEW_ENTITY),
                    shader -> {
                        RBShaders.GLOW_EDGE_SHADER = shader;
                        RBShaders.glowColor = shader.safeGetUniform("glowColor");
                        RBShaders.glowWidth = shader.safeGetUniform("glowWidth");
                        RBShaders.glowTime = shader.safeGetUniform("time");
                    });
        } catch (Exception e) {
            RenderBlenderLib.LOGGER.info("[RenderBlender] glow_edge shader 未找到（可选，不影响 cosmic 等核心效果）");
        }
    }
}