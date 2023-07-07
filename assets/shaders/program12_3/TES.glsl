#version 430

layout (quads, equal_spacing,ccw) in;

uniform mat4 mvp;
layout (binding = 0) uniform sampler2D textureMap; // Also used as height map.

out vec2 tes_out;

void main (void) {
     // Transform the oringinal [0, 1] to [-0.5, 0.5].
     vec4 tessellatedPoint = vec4(gl_TessCoord.x - .5, 0.0, gl_TessCoord.y - .5, 1.0);

     // Vertex coord's (0, 0) is at upper left, while in texture coord, it's at lower left.
     // Hence, transform the y component like this.
     vec2 tc = vec2(gl_TessCoord.x, 1.0 - gl_TessCoord.y);

     // Texture map & height map use the same texture.
     // Because the texture is grayscale, one of R or G or B can serve as the value.
     // 40.0 is for scaling down.
     float mappedHeight = texture(textureMap, tc).r / 40.0;
     tessellatedPoint.y += mappedHeight;

     // Last, convert to final space by mul mvp mat.
     gl_Position = mvp * tessellatedPoint;
     tes_out = tc;
}