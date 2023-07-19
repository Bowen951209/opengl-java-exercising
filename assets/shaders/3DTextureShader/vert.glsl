#version 430

layout (location=0) in vec3 vertPos;
out vec3 originalPosition;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout(binding = 0) uniform sampler3D sampler;


void main(void) {
	originalPosition = vertPos;
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
