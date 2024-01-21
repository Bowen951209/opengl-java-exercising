#version 430

layout(local_size_x = 4, local_size_y = 4, local_size_z = 2) in;

//layout(rgba32f, bindings = 0) uniform image3D imgOutput;
layout(rgba8, binding = 0) uniform image2D imgOutput;

uniform float scale;
uniform float layer;

// Defined in worley3D.glsl
vec2 worley(vec3 P, float jitter, bool manhattanDistance);

void main() {
    ivec2 texCoord = ivec2(gl_GlobalInvocationID.xy);
    vec2 f = worley(vec3(texCoord, layer) * scale, 1.0, false);

    vec4 color = vec4(vec3(f.x), 1.0);

    imageStore(imgOutput, texCoord, color);
}
