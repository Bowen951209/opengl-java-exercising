#version 430

uniform mat4 mvp;
layout (binding = 0) uniform sampler2D textureMap;

out vec2 tc;

void main(void) {
    vec2 patchTexCoords[] = vec2[](
        vec2(0, 0),
        vec2(1, 0),
        vec2(0, 1),
        vec2(1, 1)
    );

    // Coordinates in big collection.
    int x = gl_InstanceID % 64;
    int y = gl_InstanceID / 64; // 無條件捨去

    // Texture coordinates.
    tc = vec2((x + patchTexCoords[gl_VertexID].x) / 64.0, (63 - y + patchTexCoords[gl_VertexID].y) / 64.0); // flip y

    gl_Position = vec4(
        tc.x - .5, // [0, 1] -> [.5, .5]
        .0,
        (1.0 - tc.y) - .5,  // flip y, [0, 1] -> [.5, .5]
        1.0
    );
}