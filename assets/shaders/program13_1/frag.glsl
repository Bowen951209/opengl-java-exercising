#version 430

in vec3 varyingNormalG;
in vec3 varyingLightDirG;
in vec3 varyingVertPos;
in vec3 varyingHalfVectorG;

out vec4 fragColor;

struct PositionalLight
{    vec4 ambient;
     vec4 diffuse;
     vec4 specular;
     vec3 position;
};

struct Material
{    vec4 ambient;
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
uniform int isLighting;

void main(void)
{    // normalize the light, normal, and view vectors:(現在已經不需要計算R)
     vec3 L = normalize(varyingLightDirG);
     vec3 N = normalize(varyingNormalG);
     vec3 V = normalize(-varyingVertPos);

     float cosTheta = dot(L, N);

     vec3 H = normalize(varyingHalfVectorG);

     float cosPhi = dot(H, N);


     if (isLighting == 1) { // Front face render light.
          vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
          vec3 diffuse = light.diffuse.xyz * material.diffuse.xyz * max(cosTheta, 0.0);
          vec3 specular = light.specular.xyz * material.specular.xyz * pow(max(cosPhi, 0.0), material.shininess * 3.0);
          fragColor = vec4((ambient + diffuse + specular), 1.0);
     } else { // Backface just ambient for less prominent.
         vec3 ambient = ((globalAmbient * material.ambient) + (light.ambient * material.ambient)).xyz;
         fragColor = vec4(ambient, 1.0);
     }
}
