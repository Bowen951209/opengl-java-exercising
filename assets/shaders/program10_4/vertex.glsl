#version 430

layout (location = 0) in vec3 vertPos;
layout (location = 1) in vec3 vertNormal;
layout (location = 2) in vec2 tc;

out vec3 varyingNormal;
out vec3 varyingVertPos;
out vec2 varyingTc;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

layout(binding = 0) uniform sampler2D imageTexture;
layout(binding = 1) uniform sampler2D heightMap;

void main(void) {
    vec4 modifiedPos = vec4(vertPos, 1.0) + vec4(vertNormal * texture(heightMap, tc).r / 5.0, 1.0);
    varyingTc = tc;

    gl_Position = proj_matrix * mv_matrix * modifiedPos;
}
