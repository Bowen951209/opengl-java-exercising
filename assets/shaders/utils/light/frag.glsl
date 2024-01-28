#version 430

out vec4 fragColor;

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
    fragColor = globalAmbient + light.ambient + light.diffuse + light.specular;
}