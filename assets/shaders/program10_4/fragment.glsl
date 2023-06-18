#version 430

in vec2 varyingTc;

out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

layout(binding = 0) uniform sampler2D imageTexture;
layout(binding = 1) uniform sampler2D heightMap;

void main(void) {
    fragColor = texture(imageTexture, varyingTc);
}