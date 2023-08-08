#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 glp;

out vec4 fragColor;

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

layout(binding = 0) uniform sampler2D reflectionTexture;
layout(binding = 1) uniform sampler2D refractionTexture;

void main(void) {

    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);

    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    vec2 tcForReflection = vec2(glp.x, -glp.y) / glp.w / 2.0 + 0.5;
    vec2 tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;

    // case above water
    vec4 reflectColor = texture(reflectionTexture, tcForReflection);
    vec4 refractColor = texture(refractionTexture, tcForRefraction);
    vec4 reflectRefractMix = 0.2 * refractColor + reflectColor;

    vec3 ambient = ((globalAmbient) + (light.ambient)).xyz;
    vec3 diffuse = light.diffuse.xyz * max(cosTheta,0.0);
    vec3 specular = light.specular.xyz * pow(max(cosPhi,0.0), material.shininess);

    fragColor = vec4(reflectRefractMix.xyz * (ambient + diffuse) + 0.75 * specular, 1.0);
}
