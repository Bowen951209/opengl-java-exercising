#version 430 core
in vec2 tc; // 輸入插值過的紋理座標
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

layout (binding=0) uniform sampler2D samp;
void main()
{
    color = texture(samp, tc);
//    color = vec4(1f, 1f, 1f, 1f);
}