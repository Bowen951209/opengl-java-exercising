#version 430

in vec3 tc;
out vec4 fragColor;

uniform mat4 v_matrix;
uniform mat4 p_matrix;
layout (binding = 1) uniform samplerCube samp;

void main(void) {
    fragColor = texture(samp,tc);
//    fragColor = vec4(tc, 1);
}
