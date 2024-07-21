#version 430

layout(binding = 0) uniform sampler2D computeTexture;

out vec4 fragColor;

void main() {
    vec2 uv = gl_FragCoord.xy / textureSize(computeTexture, 0);
    fragColor = texture(computeTexture, uv);
}