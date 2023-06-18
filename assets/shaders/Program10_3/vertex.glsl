#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
layout (location = 2) in vec2 tc;
layout (location = 3) in vec3 tangent;

out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;
out vec3 varyingTangent;
out vec2 varyingTc;

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
uniform int isUsingNormalMapInInt;
uniform int isUsingImageTextureInInt;

layout(binding = 0) uniform sampler2D normMap;
layout(binding = 1) uniform sampler2D textureMap;

void main(void) {
    varyingVertPos = (mv_matrix * vec4(vertPos, 1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (norm_matrix * vec4(vertNormal, 1.0)).xyz;
    varyingHalfVector =
    normalize(normalize(varyingLightDir) + normalize(-varyingVertPos)).xyz;
    varyingTc = tc;
    varyingTangent = tangent;

    gl_Position = proj_matrix * mv_matrix * vec4(vertPos, 1.0);
}
