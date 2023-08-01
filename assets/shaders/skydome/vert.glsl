#version 430

layout (location=0) in vec3 vertPos;
out vec3 tc;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout(binding = 0) uniform sampler3D sampler;


void main(void) {
    tc = vertPos;

    mat4 mv_matrixWithNoPosition = mat4(mat3(mv_matrix));
    gl_Position = proj_matrix * mv_matrixWithNoPosition * vec4(vertPos, 1.0);
    gl_Position.y -= 0.5;
}
