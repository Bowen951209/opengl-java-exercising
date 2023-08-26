#version 430

layout (local_size_x=1) in;
layout(binding=0) buffer outputRayStart {
    float[] output_ray_start;
};
layout(binding=1) buffer outputRayDir {
    float[] output_ray_dir;
};

uniform float camera_pos_x;
uniform float camera_pos_y;
uniform float camera_pos_z;
uniform mat4 cameraToWorld_matrix;

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

    vec3 rayOriginWorld = mat3(cameraToWorld_matrix) * rayOrigin;
    vec3 rayPWorld = mat3(cameraToWorld_matrix) * vec3(Px, Py, -1f);

    Ray world_ray;
    world_ray.start = vec3(camera_pos_x, camera_pos_y, camera_pos_z);
    world_ray.dir = rayPWorld - rayOriginWorld;


    output_ray_start[index] = world_ray.start.x;
    output_ray_start[index + 1] = world_ray.start.y;
    output_ray_start[index + 2] = world_ray.start.z;

    output_ray_dir[index] = world_ray.dir.x;
    output_ray_dir[index + 1] = world_ray.dir.y;
    output_ray_dir[index + 2] = world_ray.dir.z;
}