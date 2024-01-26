#version 430
layout (location = 0) in vec3 vertPos;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform mat4 mvMat;
uniform mat4 projMat;

void main() {
    gl_Position = projMat * mvMat * vec4(vertPos, 1.0);
}