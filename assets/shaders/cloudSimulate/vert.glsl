#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
layout (location = 2) in vec2 tc;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;
out vec2 varyingTc;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material {
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mvMat;
uniform mat4 projMat;
uniform mat4 normMat;
layout (binding = 0) uniform sampler2D tex;

void main(void) {
    varyingVertPos = (mvMat * vec4(vertPos, 1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (normMat * vec4(vertNormal, 1.0)).xyz;
    varyingHalfVector = normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;

    varyingTc = tc;
    gl_Position = projMat * mvMat * vec4(vertPos, 1.0);
}
