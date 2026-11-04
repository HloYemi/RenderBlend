#version 150

uniform sampler2D Sampler0;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform float time;
uniform float externalScale;
uniform float opacity;
uniform float cosmicuvs[40];

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    if (color.a < 0.1) discard;

    // 物品 UV 空间的参考中心(用 cosmic_0 贴图 UV 范围的中心)
    // 距离中心越远,旋转角度越大  旋涡形变(区别于全局同角旋转的"平移"效果)
    vec2 center = vec2((cosmicuvs[0] + cosmicuvs[2]) * 0.5, (cosmicuvs[1] + cosmicuvs[3]) * 0.5);
    vec2 rel = texCoord0 - center;
    float dist = length(rel);

    // 旋涡核心:角度随时间推进,且距离越大角速度越快
    // 0.5 是基础转速,dist*8.0 让外圈明显比内圈转得快  视觉上的旋涡
    float swirlAngle = time * (0.5 + dist * 8.0);
    float s = sin(swirlAngle);
    float c = cos(swirlAngle);
    mat2 swirl = mat2(c, -s, s, c);
    vec2 uv = center + swirl * rel;

    vec4 cosmicColor = vec4(0.0);
    for (int i = 0; i < 10; i++) {
        vec2 minUv = vec2(cosmicuvs[i * 4], cosmicuvs[i * 4 + 1]);
        vec2 maxUv = vec2(cosmicuvs[i * 4 + 2], cosmicuvs[i * 4 + 3]);
        // 每张 cosmic 贴图独立平移,叠加后呈流动感
        vec2 sampleUv = mix(minUv, maxUv, fract(uv * externalScale + time * 0.1 + float(i) * 0.13));
        cosmicColor += texture(Sampler0, sampleUv);
    }

    cosmicColor /= 10.0;
    cosmicColor.a *= opacity;

    vec4 finalColor = mix(color, cosmicColor, cosmicColor.a * color.a);
    if (finalColor.a < 0.1) discard;
    fragColor = finalColor * ColorModulator;
}