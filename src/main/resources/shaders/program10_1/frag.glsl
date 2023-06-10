#version 430

in vec3 vNormal;
in vec3 vVertPos;
out vec4 fragColor;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
layout (binding = 1) uniform samplerCube t;

void main(void)
{
    float a = 0.4;		// controls depth of bumps
    float b = 10.0;	// controls width of bumps
    float x = vVertPos.x;
    float y = vVertPos.y;
    float z = vVertPos.z;
    vec3 N;
    N.x = vNormal.x + a*sin(b*x);
    N.y = vNormal.y + a*sin(b*y);
    N.z = vNormal.z + a*sin(b*z);


    vec3 r = -reflect(normalize(-vVertPos), normalize(N));
    fragColor = texture(t,r);
}