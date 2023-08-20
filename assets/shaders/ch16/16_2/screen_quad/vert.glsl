#version 430
layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec2 tc;

out vec2 varyingTc;

void main() {
    gl_Position = vec4(vertPos, 1.0);
    varyingTc = tc;
}