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

layout (binding = 0) uniform sampler2D normMap;

vec3 calcNewNormal() {
    //    vec3 normal = normalize(varyingNormal);
    //    vec3 tangent = normalize(varyingTangent);
    //    tangent = normalize(tangent - dot(tangent, normal) * normal);// 確保tangent 和 normal 一定垂直
    //    vec3 bitangent = cross(tangent, normal);
    //    mat3 tbn = mat3(tangent, bitangent, normal); // TBN matrix is for converting to camera space
    //    vec3 retrievedNormal = texture(normMap, varyingTc).xyz;
    //    retrievedNormal = retrievedNormal * 2.0 - 1.0; // convert from RGB space
    //    vec3 newNormal = tbn * retrievedNormal;
    //    newNormal = normalize(newNormal);
    //    return newNormal;
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

    vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
    vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta, 0.0);
    vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi, 0.0), material.shininess * 3.0);
    fragColor = vec4((ambient + diffuse + specular), 1.0);
}
