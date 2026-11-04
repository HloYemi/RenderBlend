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
    // 1. 采样原版底图
    vec4 color = texture(Sampler0, texCoord0) * vertexColor;
    
    // 2. 如果原版是透明的，直接丢弃（保留原版裁剪形状）
    if (color.a < 0.1) discard;
    
    // 3. 采样星空背景
    vec2 uv = texCoord0;
    float s = sin(time * 0.5);
    float c = cos(time * 0.5);
    mat2 rot = mat2(c, -s, s, c);
    uv = rot * uv;

    vec4 cosmicColor = vec4(0.0);
    for (int i = 0; i < 10; i++) {
        vec2 minUv = vec2(cosmicuvs[i * 4], cosmicuvs[i * 4 + 1]);
        vec2 maxUv = vec2(cosmicuvs[i * 4 + 2], cosmicuvs[i * 4 + 3]);
        vec2 sampleUv = mix(minUv, maxUv, fract(uv * externalScale + time * 0.1));
        cosmicColor += texture(Sampler0, sampleUv);
    }
    cosmicColor /= 10.0;
    
    // 4. 强制混合：原版颜色 + 星空发光
    vec4 finalColor = color + cosmicColor * opacity * 2.0; 
    finalColor.a = color.a; // 保持原版透明度剪影
    
    fragColor = finalColor * ColorModulator;
}