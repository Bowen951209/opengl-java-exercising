#version 430

layout (quads, equal_spacing, ccw) in;

uniform mat4 mvp;
layout (binding = 0) uniform sampler2D textureMap; // Also used as height map.

in vec2 tcs_out_tc[];
out vec2 tes_out;

void main(void) {
    vec4 tessellatedPoint = vec4(
            gl_in[0].gl_Position.x + gl_TessCoord.x / 64.0,
            .0,
            gl_in[0].gl_Position.z + gl_TessCoord.y / 64.0,
            1.0
    );


    vec2 tc = vec2(tcs_out_tc[0].x + gl_TessCoord.x / 64.0, tcs_out_tc[0].y + (1.0 - gl_TessCoord.y) / 64.0);

    // Texture map & height map use the same texture.
    // Because the texture is grayscale, one of R or G or B can serve as the value.
    // 40.0 is for scaling down.
    float mappedHeight = texture(textureMap, tc).r / 40.0;
    tessellatedPoint.y += mappedHeight;

    // Last, convert to final space by mul mvp mat.
    gl_Position = mvp * tessellatedPoint;
    tes_out = tc;
}