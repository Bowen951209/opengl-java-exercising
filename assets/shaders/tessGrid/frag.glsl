#version 430

in vec2 tes_out;
out vec4 color;
uniform mat4 mv_matrix;
uniform mat4 p_matrix;

layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;

void main(void) {
    vec4 texturedColor = texture(tex_color, tes_out);
    color = texturedColor;
}