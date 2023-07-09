#version 430

in vec2 tes_out;
out vec4 color;

layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 2) uniform sampler2D tex_normal;

/* ---- for lighting ---- */
in vec3 varyingVertPos;
in vec3 varyingLightDir;
struct PositionalLight
{    vec4 ambient; vec4 diffuse; vec4 specular; vec3 position; };
struct Material
{    vec4 ambient; vec4 diffuse; vec4 specular; float shininess; };
uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
/* ---------------------- */

vec3 calcNewNormal()
{
    vec3 normal = vec3(0, 1, 0);
    vec3 tangent = vec3(1, 0, 0);
    vec3 bitangent = cross(tangent, normal);
    mat3 tbn = mat3(tangent, bitangent, normal);
    vec3 retrievedNormal = texture(tex_normal, tes_out).xyz;
    retrievedNormal = retrievedNormal * 2.0 - 1.0;
    vec3 newNormal = tbn * retrievedNormal;
    newNormal = normalize(newNormal);
    return newNormal;
}

void main(void) {
    vec3 L = normalize(varyingLightDir);
    vec3 V = normalize(-varyingVertPos);

    vec3 N = calcNewNormal();

    vec3 R = normalize(reflect(-L, N));
    float cosTheta = dot(L, N);
    float cosPhi = dot(V, R);

    vec4 ambient = globalAmbient * material.ambient + light.ambient * material.ambient;

    vec4 diffuse = light.diffuse * material.diffuse * max(cosTheta, 0.0);
    vec4 specular = light.specular * material.specular * pow(max(cosPhi, 0.0), material.shininess);


    vec4 textureColor = texture(tex_color, tes_out);

    vec4 lightedColor = ambient + diffuse + specular;


    color = .5 * lightedColor + .5 * textureColor;
}