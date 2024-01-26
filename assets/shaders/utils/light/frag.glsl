#version 430

in vec3 varyingVertPos;
out vec4 fragColor;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform mat4 mvMat;
uniform mat4 projMat;

void main() {
    fragColor = light.ambient + light.diffuse + light.specular;
}