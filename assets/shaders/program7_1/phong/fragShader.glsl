#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;

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
uniform mat4 mv_matrix;	 
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;

void main(void)
{	// 正規化光照巷量、法向量、視覺向量
	vec3 L = normalize(varyingLightDir);
	vec3 N = normalize(varyingNormal);
	vec3 V = normalize(-varyingVertPos);
	
	// 計算光照向量基於N的反射向量
	vec3 R = normalize(reflect(-L, N));
	
	// 計算光照與平面法向量的角度
	float cosTheta = dot(L,N);
	
	// 計算視覺向量與反射光向量的角度
	float cosPhi = dot(V,R);

	// 按像素計算ADS分量，並合併已構建輸出顏色
	vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
	vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta,0.0);
	vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi,0.0), material.shininess);
	
	fragColor = vec4((ambient + diffuse + specular), 1.0);
}
