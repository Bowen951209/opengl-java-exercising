#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
out vec3 varyingNormal; // 視覺空間頂點法向量
out vec3 varyingLightDir; // 指向光源的向量
out vec3 varyingVertPos; // 視覺空間中的頂點位置

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

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

void main(void)
{
	// 輸出頂點位置、光照方向和法向量到光柵著色器以進行差值
	varyingVertPos = (mv_matrix * vec4(vertPos,1.0)).xyz;
	varyingLightDir = light.position - varyingVertPos;
	varyingNormal = (norm_matrix * vec4(vertNormal,1.0)).xyz;

	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
