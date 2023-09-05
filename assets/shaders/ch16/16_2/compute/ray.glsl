#version 430

layout (local_size_x=1) in;
layout(binding=0) buffer outputRayStart {
    float[] rayStart;
};
layout(binding=1) buffer outputRayDir {
    float[] rayDir;
};

uniform vec3 cameraPosition;
uniform mat4 cameraToWorldMatrix;

struct Ray {
    vec3 start;// origin
    vec3 dir;// normalized direction
};

void main() {
    int width = int(gl_NumWorkGroups.x);
    int height = int(gl_NumWorkGroups.y);
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    uint index = (gl_GlobalInvocationID.x + gl_GlobalInvocationID.y * width) * 3;


    // Algorithm thanks to https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays.html
    float aspectRatio = float(width) / float(height); // assuming width > height
    float Px = (2 * ((pixel.x + 0.5) / width) - 1) * aspectRatio;
    float Py = -(1 - 2 * ((pixel.y + 0.5) / height));
    vec3 rayOrigin = vec3(0f, 0f, 0f);

    vec3 rayOriginWorld = mat3(cameraToWorldMatrix) * rayOrigin;
    vec3 rayPWorld = mat3(cameraToWorldMatrix) * vec3(Px, Py, -1f);

    Ray worldRay;
    worldRay.start = vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z);
    worldRay.dir = rayPWorld - rayOriginWorld;


    rayStart[index] = worldRay.start.x;
    rayStart[index + 1] = worldRay.start.y;
    rayStart[index + 2] = worldRay.start.z;

    rayDir[index] = worldRay.dir.x;
    rayDir[index + 1] = worldRay.dir.y;
    rayDir[index + 2] = worldRay.dir.z;
}