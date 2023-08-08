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
uniform int isAbove;

layout(binding = 0) uniform sampler2D reflectionTexture;
layout(binding = 1) uniform sampler2D refractionTexture;

const vec4 blueColor = vec4(0.0, 0.25, 1.0, 1.0);

void main(void) {
    // -------------------lighting----------------------
    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);

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

    if (isAbove == 1) { // above water
        tcForReflection = vec2(glp.x, -glp.y) / glp.w / 2.0 + 0.5;
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;

        reflectColor = texture(reflectionTexture, tcForReflection);
        refractColor = texture(refractionTexture, tcForRefraction);
        mixColor = 0.2 * refractColor + reflectColor;
    } else { // below water
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;

        refractColor = texture(refractionTexture, tcForRefraction);
        mixColor = 1.8 * blueColor + 1.2 * refractColor;
    }


    fragColor = vec4(mixColor.xyz * (ambient + diffuse) + 0.75 * specular, 1.0);
}
