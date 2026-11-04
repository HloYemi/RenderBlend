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
    vec2 uv = texCoord0;
    float s = sin(time);
    float c = cos(time);
    mat2 rot = mat2(c, -s, s, c);
    uv = rot * uv;
    
    vec4 cosmicColor = vec4(0.0);
    for (int i = 0; i < 10; i++) {
        vec2 minUv = vec2(cosmicuvs[i * 4], cosmicuvs[i * 4 + 1]);
        vec2 maxUv = vec2(cosmicuvs[i * 4 + 2], cosmicuvs[i * 4 + 3]);
        vec2 sampleUv = mix(minUv, maxUv, fract(uv * externalScale + time * 0.1));
        // 关键：1.21.1 中 Sampler2 是光照，必须统一从 Sampler0（图集）采样
        cosmicColor += texture(Sampler0, sampleUv);
    }
    
    cosmicColor /= 10.0;
    cosmicColor.a *= opacity;
    
    vec4 finalColor = mix(color, cosmicColor, cosmicColor.a * color.a);
    if (finalColor.a < 0.1) discard;
    fragColor = finalColor * ColorModulator;
}