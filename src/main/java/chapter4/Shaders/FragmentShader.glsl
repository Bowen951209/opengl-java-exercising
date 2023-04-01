#version 430 core
out vec4 color;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
in vec4 varyingColor;
void main()
{
    color = varyingColor;
}