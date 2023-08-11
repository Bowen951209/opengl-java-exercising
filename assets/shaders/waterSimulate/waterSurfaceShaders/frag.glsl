#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 glp;
in vec2 tc;

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
uniform int isAbove;
uniform float moveFactor;

layout(binding = 0) uniform sampler2D reflectionTexture;
layout(binding = 1) uniform sampler2D refractionTexture;
layout(binding = 2) uniform sampler2D normalMap;
layout(binding = 3) uniform sampler2D dudvMap;

const vec4 blueColor = vec4(0.0, 0.25, 1.0, 1.0);
const float waveStrength = 0.02;

vec3 calcNewNormal() {
    vec3 normal = normalize(varyingNormal);
    vec3 tangent = vec3(0.0, 0.0, 1.0);
    tangent = normalize(tangent - dot(tangent, normal) * normal);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);
    vec3 retrievedNormal = texture(normalMap, vec2(tc.x + moveFactor, tc.y + moveFactor)).xyz;
    retrievedNormal = retrievedNormal * 2.0 - 1.0;
    vec3 newNormal = tbn * retrievedNormal;
    newNormal = normalize(newNormal);

    return newNormal;
}

void main(void) {
    // -------------------lighting----------------------
    vec3 L = normalize(varyingLightDir);
    //    vec3 N = normalize(varyingNormal);
    vec3 N = calcNewNormal();

    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    vec3 ambient = ((globalAmbient) + (light.ambient)).xyz;
    vec3 diffuse = light.diffuse.xyz * max(cosTheta, 0.0);
    vec3 specular = light.specular.xyz * pow(max(cosPhi, 0.0), material.shininess);

    //--------------------water reflection & refraction--
    vec2 tcForReflection, tcForRefraction;
    vec4 reflectColor, refractColor, mixColor;

    vec2 distortion = texture(dudvMap, vec2(tc.x + moveFactor, tc.y + moveFactor)).rg * 2.0 - 1.0;
    distortion *= waveStrength;

    if (isAbove == 1) { // above water
        tcForReflection = vec2(glp.x, -glp.y) / glp.w / 2.0 + 0.5;
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;

        tcForReflection += distortion;
        tcForRefraction += distortion;

        reflectColor = texture(reflectionTexture, tcForReflection);
        refractColor = texture(refractionTexture, tcForRefraction);
        mixColor = 0.2 * refractColor + reflectColor;
    } else { // below water
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;
        tcForRefraction += distortion;

        refractColor = texture(refractionTexture, tcForRefraction);
        mixColor = 1.8 * blueColor + 1.2 * refractColor;
    }


    fragColor = vec4(mixColor.xyz * (ambient + diffuse) + 0.75 * specular, 1.0);
}
