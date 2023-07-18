#version 430

in vec3 originalPosition;
out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
layout(binding = 0) uniform sampler3D sampler;

void main(void) {
	vec4 texturedColor = texture(sampler, originalPosition / 2.0 + 0.5);// [0, 1] -> [-1, 1]
	fragColor = texturedColor;
}