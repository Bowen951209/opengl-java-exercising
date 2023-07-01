#version 430

in vec2 tes_out;
out vec4 color;
uniform mat4 mvp_matrix;

layout (binding=0) uniform sampler2D tex_color;

void main(void)
{
//    color = vec4(1, 1, 1, 1);
    color = texture(tex_color, tes_out);
}
