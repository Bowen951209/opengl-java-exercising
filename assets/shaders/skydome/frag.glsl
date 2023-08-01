#version 430

in vec3 vertPos;
in vec3 tc;

out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout(binding = 0) uniform sampler3D sampler;

void main(void) {
    vec4 texturedColor = texture(sampler, vec3(tc.x, tc.y, 0.5));
    fragColor = texturedColor;
}