#version 430

in vec2 tes_out;
out vec4 color;
uniform mat4 mvp;
layout (binding=0) uniform sampler2D textureMap;

void main(void) {
    color = texture(textureMap, tes_out);
}
