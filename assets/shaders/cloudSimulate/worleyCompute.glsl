#version 430
// local size is optimize for Nvidia warp. 32 = 4 * 4 * 2. See https://nyu-cds.github.io/python-gpu/02-cuda/
layout (local_size_x = 4, local_size_y = 4, local_size_z = 2) in;

layout (rgba8, binding = 0) uniform image2D img2DOutput;
layout (rgba8, binding = 2) uniform image3D img3DOutput;

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
    ivec3 texCoord = ivec3(gl_GlobalInvocationID);
    float total = 0.0;
    float frequency = 1.0;
    float amplitude = 1.0;
    float totalAmplitude = 0;  // Used for normalizing result to 0.0 - 1.0

    for (int i = 0; i < octaves; i++) {
        total += worley(texCoord * frequency, scale) * amplitude;
        totalAmplitude += amplitude;
        amplitude *= persistence;
        frequency *= lacunarity;
    }

    float result = total / totalAmplitude;

    imageStore(img3DOutput, texCoord, vec4(vec3(result), 1.0));

    if(texCoord.z == layer)
        imageStore(img2DOutput, texCoord.xy, vec4(vec3(result), 1.0));
}
