#version 430

in vec2 tes_out;
in vec3 vertEyeSpacePos;

out vec4 color;
uniform mat4 mv_matrix;
uniform mat4 p_matrix;

layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;

void main(void) {
    vec4 fogColor = vec4(0.7, 0.8, 0.9, 1.0); // bluish gray
    float fogStart = 0.2;
    float fogEnd = 5.0;

    float distFromCamera = length(vertEyeSpacePos.xyz);
    float fogFactor = clamp((fogEnd - distFromCamera) / (fogEnd - fogStart), 0.0, 1.0); // number to [0, 1]

    vec4 texturedColor = texture(tex_color, tes_out);

    color = mix(fogColor, texturedColor, fogFactor);
}