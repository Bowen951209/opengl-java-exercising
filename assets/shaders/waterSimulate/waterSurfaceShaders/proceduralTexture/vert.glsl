#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
layout (location = 2) in vec2 textureCoord;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;
out vec4 glp;
out vec2 tc;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform int isAbove;
uniform float moveFactor;

layout(binding = 0) uniform sampler2D reflectionTexture;
layout(binding = 1) uniform sampler2D refractionTexture;
layout(binding = 2) uniform sampler2D normalMap;

void main(void) {
    varyingVertPos = (mv_matrix * vec4(vertPos, 1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (norm_matrix * vec4(vertNormal, 1.0)).xyz;
    varyingHalfVector =
    normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;

    tc = textureCoord;
    glp = proj_matrix * mv_matrix * vec4(vertPos, 1.0);
    gl_Position = glp;
}
