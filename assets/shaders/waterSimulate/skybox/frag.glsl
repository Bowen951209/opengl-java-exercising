#version 430

in vec3 tc;
in float positionY;

out vec4 fragColor;

uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform int isAbove;
layout (binding = 0) uniform samplerCube samp;

void main(void) {
    if (isAbove != 1 && positionY < 0.1) { // below
        fragColor = vec4(0, 0, .2, 1);
    } else { // above
        // go normal
        fragColor = texture(samp, tc);
    }
}
