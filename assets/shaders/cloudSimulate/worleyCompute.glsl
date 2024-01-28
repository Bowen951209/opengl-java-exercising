#version 430
// local size is optimize for Nvidia warp. 32 = 4 * 4 * 2. See https://nyu-cds.github.io/python-gpu/02-cuda/
layout (local_size_x = 4, local_size_y = 4, local_size_z = 2) in;

layout (rgba8, binding = 0) uniform image2D imgOutput;

uniform float scale;
uniform int layer;
uniform int octaves;
uniform float persistence;
uniform float lacunarity;

// Defined in worley3D.glsl
float worley(vec3 p, float scale);

float random(float x) {
    return fract(sin(x) * 100000.0);
}

void main() {
    ivec2 texCoord = ivec2(gl_GlobalInvocationID.xy);
    float total = 0.0;
    float frequency = 1.0;
    float amplitude = 1.0;
    float totalAmplitude = 0;  // Used for normalizing result to 0.0 - 1.0

    for (int i = 0; i < octaves; i++) {
        total += worley(vec3(texCoord, layer) * frequency, scale) * amplitude;
        totalAmplitude += amplitude;
        amplitude *= persistence;
        frequency *= lacunarity;
    }

    float result = total / totalAmplitude;
    imageStore(imgOutput, texCoord, vec4(vec3(result), 1.0));
}
