#version 150

uniform sampler2D Sampler0;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform vec4 GlowColor;
uniform float GlowWidth;
uniform vec2 TexelSize;
uniform float time;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

// 采样指定偏移处的 alpha,坐标越界时按 alpha=0 处理(防止 sprite 外采样拿到邻居数据)
float sampleAlpha(vec2 uv) {
    if (uv.x < 0.0 || uv.x > 1.0 || uv.y < 0.0 || uv.y > 1.0) {
        return 0.0;
    }
    return texture(Sampler0, uv).a;
}

void main() {
    vec2 uv = texCoord0;
    float centerAlpha = sampleAlpha(uv);

    // 8 方向采样(距离 = GlowWidth 个像素),检测边缘
    // 邻居不透明 / 当前透明  外侧边缘;邻居透明 / 当前不透明  内侧边缘
    // 两者都算"边缘",输出 glowColor 让轮廓发光
    vec2 texel = TexelSize * GlowWidth;

    float outerEdge = 0.0; // 邻居不透明数(用于外侧发光)
    float innerEdge = 0.0; // 邻居透明数(用于内侧描边)
    for (int i = 0; i < 8; i++) {
        // 8 方向单位向量
        float angle = float(i) * 3.14159265 / 4.0;
        vec2 dir = vec2(cos(angle), sin(angle));
        float nAlpha = sampleAlpha(uv + dir * texel);
        if (nAlpha > 0.5) outerEdge += 1.0;
        if (nAlpha < 0.5) innerEdge += 1.0;
    }

    // 外侧边缘:当前透明 + 邻居有不透明
    // 内侧边缘:当前不透明 + 邻居有透明
    bool isOuter = (centerAlpha < 0.5 && outerEdge > 0.0);
    bool isInner = (centerAlpha > 0.5 && innerEdge > 0.0);

    if (!isOuter && !isInner) {
        discard;
    }

    // 描边色 = GlowColor 与 vertexColor 混合(顶点色可携带额外调制)
    vec4 finalColor = GlowColor * vertexColor;
    // 发光脉动:随 time 微调 alpha(0.7~1.0)
    finalColor.a *= 0.7 + 0.3 * (0.5 + 0.5 * sin(time * 3.0));
    fragColor = finalColor * ColorModulator;
}