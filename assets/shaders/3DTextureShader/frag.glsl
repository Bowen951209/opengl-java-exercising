#version 430

in vec3 originalPosition;
in vec3 vNormal, vLightDir, vVertPos, vHalfVec;

out vec4 fragColor;

struct PositionalLight
{ vec4 ambient, diffuse, specular;
    vec3 position;
};
struct Material
{ vec4 ambient, diffuse, specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
layout(binding = 0) uniform sampler3D sampler;

void main(void) {
    vec3 L = normalize(vLightDir);
    vec3 N = normalize(vNormal);
    vec3 V = normalize(-vVertPos);
    vec3 H = normalize(vHalfVec);
    float cosTheta = max(dot(L, N), 0.0);

    vec4 texturedColor = texture(sampler, originalPosition / 2.0 + 0.25);

    fragColor = 0.7 * texturedColor * (globalAmbient + light.ambient + light.diffuse * cosTheta)
    + 0.5 * light.specular * pow(cosTheta, material.shininess);
}