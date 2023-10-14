#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec2 tc;

out vec4 fragColor;

struct PositionalLight
{
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};

struct Material
{
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

layout (binding=0) uniform sampler2D samp;


void main(void) {
    vec3 L = normalize(varyingLightDir);
    vec3 N = normalize(varyingNormal);
    vec3 V = normalize(-varyingVertPos);

    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);

    float cosPhi = dot(H, N);

    // 此處改成不參考material
    vec4 ambient = globalAmbient + light.ambient;
    vec4 diffuse = light.diffuse*max(dot(L, N), 0.0);
    vec4 specular = light.specular * pow(max(cosPhi, 0.0), material.shininess*3.0);

    vec4 lightColor = ambient + diffuse + specular;
    vec4 texColor = texture(samp, tc);
    fragColor = texColor * lightColor;
}
