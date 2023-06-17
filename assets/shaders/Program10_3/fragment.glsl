#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec3 varyingTangent;
in vec2 varyingTc;

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

layout(binding = 0) uniform sampler2D normMap;
layout(binding = 1) uniform sampler2D textureMap;

vec3 calcNewNormal() {
    vec3 normal = normalize(varyingNormal);
    vec3 tangent = normalize(varyingTangent);
    tangent = normalize(tangent - dot(tangent, normal) * normal);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);
    vec3 retrievedNormal = texture(normMap, varyingTc).xyz;
    retrievedNormal = retrievedNormal * 2.0 - 1.0;
    vec3 newNormal = tbn * retrievedNormal;
    newNormal = normalize(newNormal);

    return newNormal;
}

void main(void) {
    // modify from blinn phong shader

    vec3 L = normalize(varyingLightDir);
    //    vec3 N = normalize(varyingNormal);
    vec3 N = calcNewNormal();
    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    // standard texture
    vec4 texel = texture(textureMap, varyingTc);

    fragColor = globalAmbient
            + texel * (light.ambient + light.diffuse * max(cosTheta, 0.0))
            + light.specular * pow(max(cosPhi, 0.0), material.shininess);

//    fragColor =globalAmbient + texel * (light.ambient + light.diffuse * max(cosTheta, 0.0));
}
