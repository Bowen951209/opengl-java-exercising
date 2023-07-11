#version 430

in vec3 varyingNormalG;
in vec3 varyingLightDirG;
in vec3 varyingVertPos;
in vec3 varyingHalfVectorG;

out vec4 fragColor;

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
uniform mat4 m_matrix;
uniform mat4 v_matrix;
uniform mat4 p_matrix;
uniform mat4 norm_matrix;
uniform float inflateValue; // 膨脹

void main(void)
{	// normalize the light, normal, and view vectors:(現在已經不需要計算R)
	vec3 L = normalize(varyingLightDirG);
	vec3 N = normalize(varyingNormalG);
	vec3 V = normalize(-varyingVertPos);
	
	// get the angle between the light and surface normal:
	float cosTheta = dot(L,N);
	
	// halfway vector varyingHalfVector was computed in the vertex shader,
	// and interpolated prior to reaching the fragment shader.
	// It is copied into variable H here for convenience later.
	vec3 H = normalize(varyingHalfVectorG);
	
	// get angle between the normal and the halfway vector
	float cosPhi = dot(H,N);

	// compute ADS contributions (per pixel):
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess*3.0);
	// 最後*3.0作為改善鏡面高光的微調
	fragColor = vec4((ambient + diffuse + specular), 1.0);
}
