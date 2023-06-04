#version 430

out vec4 fragColor;

uniform mat4 proj_matrix;
uniform mat4 mv_matrix;

void main(void) {
    fragColor = vec4(0, 1, 0, 1.0);
}
