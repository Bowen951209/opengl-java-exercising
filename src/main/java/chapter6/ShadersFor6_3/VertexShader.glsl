#version 430 core
layout (location=0) in vec3 position;
layout (location=1) in vec2 texCoord;

uniform mat4 mv_matrix;
uniform mat4 proj_matrix;

out vec2 tc; //紋理座標輸出到光柵著色氣用於插值

layout (binding=0) uniform sampler2D samp; // 頂點著色器中未使用

void main(void) {

    gl_Position = proj_matrix * mv_matrix * vec4(position, 1.0);
    tc = texCoord;
}



































