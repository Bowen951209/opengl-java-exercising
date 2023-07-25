#version 430

layout (location=0) in vec3 vertPos;
layout (location=1) in vec3 vertNormal;

out vec3 originalPosition;
out vec3 vNormal, vLightDir, vVertPos, vHalfVec;

struct PositionalLight
{	vec4 ambient, diffuse, specular;
	vec3 position;
};
struct Material
{	vec4 ambient, diffuse, specular;
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
	vVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	vLightDir = light.position - vVertPos;
	vNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;
	vHalfVec = (vLightDir-vVertPos).xyz;

	originalPosition = vertPos;
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
