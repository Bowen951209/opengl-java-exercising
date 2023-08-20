#version 430
in vec2 varyingTc;
out vec4 fragColor;

layout(binding = 0) uniform sampler2D sampler;

void main() {
    fragColor = texture(sampler, varyingTc);
}