#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
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
uniform int isAbove;
uniform float moveFactor;
uniform float causticSampleY;

layout(binding = 2) uniform sampler2D normalMap;
layout(binding = 4) uniform sampler3D noiseTex;

vec3 checkerboard(vec2 tc) {
    float tileScale = 32.0;
    float tile = mod(floor(tc.x * tileScale) + floor(tc.y * tileScale), 2.0);

    return tile * vec3(1, 1, 1);
}

const vec4 blueColor = vec4(0.0, 0.25, 1.0, 1.0);
const vec4 fogColor = vec4(0.0, 0.0, 0.2, 1.0);
const float fogStart = 10.0;
const float fogEnd = 300.0;

vec3 calcNewNormal() {
    vec3 normal = normalize(varyingNormal);
    vec3 tangent = vec3(0.0, 0.0, 1.0);
    tangent = normalize(tangent - dot(tangent, normal) * normal);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);
    vec3 retrievedNormal = texture(normalMap, vec2(varyingTc.x + moveFactor, varyingTc.y + moveFactor)).xyz;
    retrievedNormal = retrievedNormal * 2.0 - 1.0;
    vec3 newNormal = tbn * retrievedNormal;
    newNormal = normalize(newNormal);

    return newNormal;
}

float getCausticValue(float x, float y, float z){
    const float SCALE = 0.2;

    float w = 8;  // frequency of caustic ribbon patterns
    float strength = 4.0;
    float PI = 3.14159;
    float noise = texture(noiseTex, vec3(x*w * SCALE,  y, z*w * SCALE)).r;
    return pow((1.0-abs(sin(noise*2*PI))), strength);
}

void main(void) {

    vec3 L = normalize(varyingLightDir);
    vec3 N = calcNewNormal();

    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    // standard texture
    vec4 texel = vec4(checkerboard(varyingTc), 1.0);

    fragColor = globalAmbient
    + texel * (light.ambient + light.diffuse * max(cosTheta, 0.0))
    + light.specular * pow(max(cosPhi, 0.0), material.shininess);

    if(isAbove != 1) { // below
        // caustic
        float causticColor = getCausticValue(varyingTc.s, causticSampleY, varyingTc.t);
        float colorR = clamp(fragColor.x + causticColor, 0.0, 1.0);
        float colorG = clamp(fragColor.y + causticColor, 0.0, 1.0);
        float colorB = clamp(fragColor.z + causticColor, 0.0, 1.0);
        fragColor = vec4(colorR, colorG, colorB, 1.0);

        // fog
        float dist = length(varyingVertPos);
        float fogFactor = clamp((fogEnd - dist) / (fogEnd-fogStart), 0.0, 1.0);

        fragColor = mix(fragColor, blueColor, 0.2);
        fragColor = mix(fogColor, fragColor, pow(fogFactor, 5));
    }
}
