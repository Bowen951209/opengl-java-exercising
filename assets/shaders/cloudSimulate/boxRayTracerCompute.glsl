#version 430
// local size is optimize for Nvidia warp. 32 = 8 * 4 * 1. See https://nyu-cds.github.io/python-gpu/02-cuda/
layout (local_size_x = 8, local_size_y = 4, local_size_z = 1) in;

const float FOV = 1.0472f; // 60 deg

layout (binding = 1, rgba8) uniform image2D outputTexture;
uniform vec3 boxMin;
uniform vec3 boxMax;
uniform mat4 invVMat;

struct Ray {
    vec3 o;
    vec3 dir;// normalized direction
};

/**
  *  @param boundsMin -- the min bounds of the box.
  *  @param boundsMax -- the max bounds of the box.
  *  @parma rayOrigin -- the origin of the ray.
  *  @parma invRayDir -- the inverse of the ray dir. (as 1 / rayDir)
  *  @returns (dstToBox, dstInsideBox). If ray misses box, dstInsideBox will be zero
*/
vec2 rayBoxDst(vec3 boundsMin, vec3 boundsMax, vec3 rayOrigin, vec3 invRayDir) {
    // Adpated from https://github.com/SebLague/Clouds/tree/fcc997c40d36c7bedf95a294cd2136b8c5127009
    vec3 t0 = (boundsMin - rayOrigin) * invRayDir;
    vec3 t1 = (boundsMax - rayOrigin) * invRayDir;
    vec3 tmin = min(t0, t1);
    vec3 tmax = max(t0, t1);

    float dstA = max(max(tmin.x, tmin.y), tmin.z);
    float dstB = min(tmax.x, min(tmax.y, tmax.z));

    // CASE 1: ray intersects box from outside (0 <= dstA <= dstB)
    // dstA is dst to nearest intersection, dstB dst to far intersection

    // CASE 2: ray intersects box from inside (dstA < 0 < dstB)
    // dstA is the dst to intersection behind the ray, dstB is dst to forward intersection

    // CASE 3: ray misses box (dstA > dstB)

    float dstToBox = max(0, dstA);
    float dstInsideBox = max(0, dstB - dstToBox);
    return vec2(dstToBox, dstInsideBox);
}

/**
    Get the ray from the camera to the pixel this call is at.
*/
Ray getCamToPixRay() {
    // Adapt from https://www.scratchapixel.com/lessons/3d-basic-rendering/ray-tracing-generating-camera-rays/generating-camera-rays.html
    vec2 coord = gl_GlobalInvocationID.xy;
    uint width = gl_NumWorkGroups.x * gl_WorkGroupSize.x;
    uint height = gl_NumWorkGroups.y * gl_WorkGroupSize.y;
    float aspectRatio = float(width) / float(height);
    // Scaling the px from [0, width] to [-aspectRatio, aspectRatio].
    // Scaling the py from [0, height] to [-1, 1].
    float px = (2.0 * ((coord.x + 0.5) / width) - 1) * tan(FOV / 2.0) * aspectRatio;
    float py = (1.0 - 2.0 * ((coord.y + 0.5) / height) * tan(FOV / 2.0));

    // Then apply the invVMat(camera-to-world) to world space.
    vec4 rayOrigin = vec4(0.0, 0.0, 0.0, 1.0);
    vec3 rayOriginWorld = (invVMat * rayOrigin).xyz;
    vec3 rayPWorld = (invVMat * vec4(px, py, -1.0, 1.0)).xyz;

    Ray worldRay;
    worldRay.o = rayOriginWorld;
    worldRay.dir = normalize(rayPWorld - rayOriginWorld);

    return worldRay;
}

void main() {
    Ray camToPixRay= getCamToPixRay();

    vec2 hitInfo = rayBoxDst(boxMin, boxMax, camToPixRay.o, 1 / camToPixRay.dir);
    float dstToBox = hitInfo.x;
    float dstInsideBox = hitInfo.y;
    bool rayHitBox = dstInsideBox > 0;

    vec3 color;
    if (rayHitBox)
        color = vec3(1.0, 1.0, 1.0);
    else
        color = vec3(0.0, 0.0, 0.0);

    imageStore(outputTexture, ivec2(gl_GlobalInvocationID.xy), vec4(color, 1.0));
}
