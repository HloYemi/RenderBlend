#version 150

uniform sampler2D Sampler0;
uniform sampler2D Sampler2;
uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform vec4 ColorModulator;
uniform float time;
uniform float yaw;
uniform float pitch;
uniform float externalScale;
uniform float opacity;
uniform float cosmicuvs[40];

in vec2 texCoord0;
in vec4 vertexColor;
in vec3 pos;

out vec4 fragColor;

mat2 rotate(float a) {
    return mat2(cos(a), -sin(a), sin(a), cos(a));
}

void main() {
    vec4 color = texture(Sampler0, texCoord0) * vertexColor * ColorModulator;
    if (color.a < 0.1) discard;
    
    vec2 uv = texCoord0;
    float s = sin(time);
    float c = cos(time);
    mat2 rot = mat2(c, -s, s, c);
    uv = rot * uv;
    
    vec4 cosmicColor = vec4(0.0);
    for (int i = 0; i < 10; i++) {
        vec2 uv0 = vec2(cosmicuvs[i * 4], cosmicuvs[i * 4 + 1]);
        vec2 uv1 = vec2(cosmicuvs[i * 4 + 2], cosmicuvs[i * 4 + 3]);
        vec2 minUv = min(uv0, uv1);
        vec2 maxUv = max(uv0, uv1);
        
        vec2 sampleUv = mix(minUv, maxUv, fract(uv * externalScale + time * 0.1));
        cosmicColor += texture(Sampler0, sampleUv);
    }
    
    cosmicColor /= 10.0;
    cosmicColor.a *= opacity;
    
    fragColor = mix(color, cosmicColor, cosmicColor.a * color.a);
}