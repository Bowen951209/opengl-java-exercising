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

vec3 checkerboard(vec2 tc) {
    float tileScale = 32.0;
    float tile = mod(floor(tc.x * tileScale) + floor(tc.y * tileScale), 2.0);

    return tile * vec3(1, 1, 1);
}

void main(void) {

    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);

    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    // standard texture
    vec4 texel = vec4(checkerboard(varyingTc), 1.0);

    fragColor = globalAmbient
    + texel * (light.ambient + light.diffuse * max(cosTheta, 0.0))
    + light.specular * pow(max(cosPhi, 0.0), material.shininess);
}
