#version 430

// See this for configuring local size: https://computergraphics.stackexchange.com/questions/13449/opengl-compute-local-size-vs-performance
layout (local_size_x = 8, local_size_y = 4, local_size_z = 1) in;

#define MAX_STEPS 100
const float MARCH_SIZE = 0.08;

layout(binding = 0, rgba8) uniform image2D outputTexture;
layout(binding = 1) uniform sampler2D noiseTexture;

uniform float uTime;
uniform vec3 camPos;
uniform mat4 invViewMat;

struct Ray {
    vec3 o;// origin
    vec3 dir;// normalized direction
};

float noise(vec3 x) {
    vec3 p = floor(x);
    vec3 f = fract(x);
    vec3 u = f * f * (3.0 - 2.0 * f);

    vec2 uv = (p.xy + vec2(37.0, 239.0) * p.z) + u.xy;
    vec2 tex = textureLod(noiseTexture,(uv + 0.5) / 256.0, 0.0).yx;

    return mix(tex.x, tex.y, u.z) * 2.0 - 1.0;
}

float fbm(vec3 p) {
    vec3 q = p + uTime * 0.5 * vec3(1.0, -0.2, -1.0);
    float g = noise(q);

    float f = 0.0;
    float scale = 0.5;
    float factor = 2.02;

    for (int i = 0; i < 6; i++) {
        f += scale * noise(q);
        q *= factor;
        factor += 0.21;
        scale *= 0.5;
    }

    return f;
}

// Get singed distance field from p to the sphere at (0, 0, 0) with the given radius.
float sdSphere(vec3 p, float radius) {
    return length(p) - radius;
}

float density(vec3 p) {
    float distance = sdSphere(p, 1.0);
    float f = fbm(p);

    return -distance + f;
}

vec4 raymarch(Ray ray) {
    float depth = 0.0;
    vec3 p = ray.o;

    vec4 res = vec4(0.0);

    for (int i = 0; i < MAX_STEPS; i++) {
        float density = density(p);

        // We only draw the density if it's greater than 0
        if (density > 0.0) {
            vec4 color = vec4(mix(vec3(1.0), vec3(0.0), density), density);
            color.rgb *= color.a;
            res += color * (1.0-res.a);
        }

        depth += MARCH_SIZE;
        p = ray.o + depth * ray.dir;
    }

    return res;
}

Ray getWorldRay(ivec2 texel) {
    // Algorithm thanks to https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays.html
    ivec2 imgSize = imageSize(outputTexture);
    float width = imgSize.x;
    float height = imgSize.y;

    float aspectRatio = width / height; // assuming width > height
    float Px = (2 * ((texel.x + 0.5) / width) - 1) * aspectRatio;
    float Py = -(1 - 2 * ((texel.y + 0.5) / height));
    vec3 rayOrigin = vec3(0.0, 0.0, 0.0);

    vec3 rayOriginWorld = mat3(invViewMat) * rayOrigin;
    vec3 rayPWorld = mat3(invViewMat) * vec3(Px, Py, -1.0);

    Ray worldRay;
    worldRay.o = vec3(camPos.x, camPos.y, camPos.z);
    worldRay.dir = normalize(rayPWorld - rayOriginWorld);
    return worldRay;
}

void main() {
    ivec2 textl = ivec2(gl_GlobalInvocationID.xy);

    Ray worldRay = getWorldRay(textl);
    vec4 color = raymarch(worldRay);

//    ivec2 imgSize = imageSize(outputTexture);
//    vec4 color = vec4(float(textl.x) / float(imgSize.x), float(textl.y) / float(imgSize.y), 0.0, 1.0);

    imageStore(outputTexture, textl, color);
}