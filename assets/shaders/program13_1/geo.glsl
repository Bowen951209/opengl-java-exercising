#version 430

// Inputs from vertex shader
layout(triangles) in;
in vec3 varyingNormal[];
in vec3 varyingLightDir[];
in vec3 varyingHalfVector[];

// Outputs through rasterizer to fragment shader.
layout(triangle_strip, max_vertices = 3) out;
out vec3 varyingNormalG;
out vec3 varyingLightDirG;
out vec3 varyingHalfVectorG;

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

void main(void) {
     // A triangle's normal = average of 3 vertices' normals.
     vec4 triangleNormal = vec4((varyingNormal[0] + varyingNormal[1] + varyingNormal[2]) / 3.0, 1.0);

     // Move vertices along the normal
     for(int i = 0; i < 3; i++) {
          // in point -> move -> view space
          vec4 movedPoint = gl_in[i].gl_Position + normalize(triangleNormal) * inflateValue;
          gl_Position = p_matrix * v_matrix * movedPoint;

          varyingNormalG = varyingNormal[i];
          varyingLightDirG = varyingLightDir[i];
          varyingHalfVectorG = varyingHalfVector[i];
          EmitVertex();
     }
     EndPrimitive();
}