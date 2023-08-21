#version 430
layout(local_size_x = 1) in;
layout(binding = 0, rgba8) uniform image2D output_texture;
float camera_pos_z = 5.0;

struct Ray {
    vec3 start;// origin
    vec3 dir;// normalized direction
};

struct Collision {
    float t;// distance from ray's origin to collision point
    vec3 p;// world position
    vec3 n;// normal at the collision point
    bool isInside;// whether ray started inside an object and collided
    int object_index;// index of the object that the ray hits
};

// Defining Models
// sphere
float sphere_radius = 2.5;
vec3 sphere_position = vec3(1.0, 0.0, -3.0);
vec3 sphere_color = vec3(0.0, 0.0, 1.0);// blue

// Box
vec3 box_mins = vec3(-2.0, -2.0, 0.0);// a corner of the box
vec3 box_maxs = vec3(-0.5, 1.0, 2.0);// a corner of the box
vec3 box_color = vec3(1.0, 0.0, 0.0);// red

// ----------------------------Check if the ray hit the box----------------------------
Collision intersect_box_object(Ray ray) {
    // calculate the box's mins and maxs
    vec3 t_min = (box_mins - ray.start) / ray.dir;
    vec3 t_max = (box_maxs - ray.start) / ray.dir;
    vec3 t_minDist = min(t_min, t_max);
    vec3 t_maxDist = max(t_min, t_max);
    float t_near = max(max(t_minDist.x, t_minDist.y), t_minDist.z);
    float t_far = min(min(t_maxDist.x, t_maxDist.y), t_maxDist.z);

    Collision collision;
    collision.t = t_near;
    collision.isInside = false;

    // if the ray didn't hit the box, return a negative t value
    if (t_near > t_far || t_far < 0.0) {
        collision.t = -1.0;
        return collision;
    }

    float intersect_distance = t_near;
    vec3 plane_intersect_distances = t_minDist;

    // if t_near < 0, then the ray started inside the box and left
    if (t_near < 0.0) {
        collision.t = t_far;
        intersect_distance = t_far;
        plane_intersect_distances = t_maxDist;
        collision.isInside = true;
    }

    // check which boundary the ray hits
    int face_index = 0;
    if (intersect_distance == plane_intersect_distances.y) {
        face_index = 1;
    } else if (intersect_distance == plane_intersect_distances.z) {
        face_index = 2;
    }

    // create collision normal
    collision.n = vec3(0.0);
    collision.n[face_index] = 1.0;
    // if hit the box from the negative axis, invert the normal
    if (ray.dir[face_index] > 0.0) {
        collision.n *= -1.0;
    }
    // calculate the world position of the hit point
    collision.p = ray.start + collision.t * ray.dir;
    return collision;
}

// ---------------------------- Check if ray hit the sphere ----------------------------
Collision intersect_sphere_object(Ray ray) {
    float qa = dot(ray.dir, ray.dir);
    float qb = dot(2.0 * ray.dir, ray.start - sphere_position);
    float qc = dot(ray.start - sphere_position, ray.start - sphere_position)
    - sphere_radius * sphere_radius;

    // solving for qa * t * t + qb * t + qc = 0
    float qd = qb * qb - 4 * qa * qc;

    Collision collision;
    collision.isInside = false;

    if (qd < 0.0) { // no solution in this case
        collision.t = -1.0;
        return collision;
    }

    float t1 = (-qb + sqrt(qd)) / (2.0 * qa);
    float t2 = (-qb - sqrt(qd)) / (2.0 * qa);
    float t_near = min(t1, t2);
    float t_far = max(t1, t2);
    collision.t = t_near;

    if (t_far < 0.0) { // sphere is behind the ray, no inersection
        collision.t = -1.0;
        return collision;
    }

    if (t_near < 0.0) { // ray started inside the sphere
        collision.t = t_far;
        collision.isInside = true;
    }

    collision.p = ray.start + collision.t * ray.dir;// world position if the hit point
    collision.n = normalize(collision.p - sphere_position);

    if (collision.isInside) { // if ray is inside, flip the normal
        collision.n *= -1.0;
    }

    return collision;
}



/*
Returns the closet collision of the ray
object_index == -1 -> no collision
object_index == 1 -> collision with sphere
object_index == 2 -> collision with box
*/

Collision get_closet_collision(Ray ray) {
    Collision closest_collision, cSphere, cBox;
    closest_collision.object_index = -1;

    cSphere = intersect_sphere_object(ray);
    cBox = intersect_box_object(ray);

    if ((cSphere.t > 0) && ((cSphere.t < cBox.t) || (cBox.t < 0))) {
        closest_collision = cSphere;
        closest_collision.object_index = 1;
    }
    if ((cBox.t > 0) && ((cBox.t < cSphere.t) || (cSphere.t < 0))) {
        closest_collision = cBox;
        closest_collision.object_index = 2;
    }

    return closest_collision;
}

vec3 raytrace(Ray ray) {
    Collision collision = get_closet_collision(ray);
    if (collision.object_index == -1) { // no collision
        return vec3(0.0);// black
    }
    if (collision.object_index == 1) {
        return sphere_color;
    }
    if (collision.object_index == 2) {
        return box_color;
    }

    return vec3(1.0, 1.0, 1.0);// error white
}

void main() {
    int width = int(gl_NumWorkGroups.x);
    int height = int(gl_NumWorkGroups.y);
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);

    // conver this screen space location to world space
    float x_pixel = 2.0 * pixel.x / width - 1.0;
    float y_pixel = 2.0 * pixel.y / height - 1.0;

    // get this pixel's world-space ray
    Ray world_ray;
    world_ray.start = vec3(0.0, 0.0, camera_pos_z);
    vec4 world_ray_end = vec4(x_pixel, y_pixel, camera_pos_z - 1.0, 1.0);
    world_ray.dir = normalize(world_ray_end.xyz - world_ray.start);

    vec3 color = raytrace(world_ray);
    imageStore(output_texture, pixel, vec4(color, 1.0));
}
