#version 430 core
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;
layout (location=2) in vec3 normal;
out vec3 varyingNormal;
out vec3 varyingLightDir;
out vec3 varyingVertPos;
out vec3 varyingHalfVector;

struct PositionalLight
{	vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};
struct Material
{	vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform vec4 globalAmbient;

uniform PositionalLight light;
uniform Material material;

out vec2 tc; //紋理座標輸出到光柵著色氣用於插值

layout (binding=0) uniform sampler2D samp; // 頂點著色器中未使用


void main(void) {
    varyingVertPos = (mv_matrix * vec4(position,1.0)).xyz;
    varyingLightDir = light.position - varyingVertPos;
    varyingNormal = (norm_matrix * vec4(normal,1.0)).xyz;
    varyingHalfVector =
    normalize(normalize(varyingLightDir)
    + normalize(-varyingVertPos)).xyz;

    gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
    tc = texCoord;
}



































