#version 430

uniform mat4 mvp_matrix;

void main(void) {
    if (gl_VertexID == 0) {
        gl_Position = vec4(-.5, -.5, 0, 1);
    } else if(gl_VertexID == 1) {
        gl_Position = vec4(.5, -.5, 0, 1);
    } else if(gl_VertexID == 2) {
        gl_Position = vec4(.5, .5, 0, 1);
    } else if(gl_VertexID == 3) {
        gl_Position = vec4(-.5, .5, 0, 1);
    }
}