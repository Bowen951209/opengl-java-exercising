#version 430
layout(local_size_x = 1) in;
layout(binding = 0, rgba8) uniform image2D output_texture;
layout(binding = 0) uniform sampler2D earthTexture;
layout(binding = 1) uniform sampler2D brickTexture;

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
    vec2 tc; // texture coordinate of the collision point
};

const float PI = 3.1415926535897932384626433832795;
const float DEG_TO_RAD = PI / 180.0;

// Defining Models
// sphere
const float sphere_radius = 2.5;
const vec3 sphere_position = vec3(0.5, 0.0, -3.0);
const vec3 sphere_color = vec3(0.0, 0.0, 1.0);// blue

// Box
const vec3 box_mins = vec3(-.5, -.5, -1.0);// a corner of the box
const vec3 box_maxs = vec3(.5, .5, 1.0);// a corner of the box
const vec3 box_color = vec3(1.0, 0.0, 0.0);// red
const vec3 box_position = vec3(-1.0, -.5, 1.0);
const float box_x_rotate = 10.0;
const float box_y_rotate = 70.0;
const float box_z_rotate = 55.0;

// Light
const vec4 global_ambient = vec4(.3, .3, .3, 1.0);
const vec4 material_ambient = vec4(.2, .2, .2, 1.0);
const vec4 material_diffuse = vec4(.7, .7, .7, 1.0);
const vec4 material_specular = vec4(1.0, 1.0, 1.0, 1.0);
const float material_shininess = 50.0;
const vec3 light_position = vec3(-4.0, 1.0, 8.0);
const vec4 light_ambient = vec4(.2, .2, .2, 1.0);
const vec4 light_diffuse = vec4(.7, .7, .7, 1.0);
const vec4 light_specular = vec4(1.0, 1.0, 1.0, 1.0);


mat4 buildTranslate(vec3 position){
    return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, 1.0, 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    position.x, position.y, position.z, 1.0
    );
}
mat4 buildRotateX(float rad){
    return mat4(
    1.0, 0.0, 0.0, 0.0,
    0.0, cos(rad), sin(rad), 0.0,
    0.0, -sin(rad), cos(rad), 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}
mat4 buildRotateY(float rad){
    return mat4(
    cos(rad), 0.0, -sin(rad), 0.0,
    0.0, 1.0, 0.0, 0.0,
    sin(rad), 0.0, cos(rad), 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}
mat4 buildRotateZ(float rad){
    return mat4(
    cos(rad), sin(rad), 0.0, 0.0,
    -sin(rad), cos(rad), 0.0, 0.0,
    0.0, 0.0, 1.0, 0.0,
    0.0, 0.0, 0.0, 1.0
    );
}

mat4 buildRotation(float xRad, float yRad, float zRad) {
    return buildRotateX(xRad) * buildRotateY(yRad) * buildRotateZ(zRad);
}


// ----------------------------Check if the ray hit the box----------------------------
Collision intersect_box_object(Ray ray) {
    mat4 model_translation = buildTranslate(box_position);
    mat4 model_rotation = buildRotation(
    box_x_rotate * DEG_TO_RAD,
    box_y_rotate * DEG_TO_RAD,
    box_z_rotate * DEG_TO_RAD
    );

    mat4 local_to_world_matrix = model_translation * model_rotation;
    mat4 world_to_local_matrix = inverse(local_to_world_matrix);
    mat4 world_to_local_rotation_matrix = inverse(model_rotation);

    vec3 ray_start = (world_to_local_matrix * vec4(ray.start, 1.0)).xyz;
    vec3 ray_dir = (world_to_local_rotation_matrix * vec4(ray.dir, 1.0)).xyz;


    // calculate the box's mins and maxs
    vec3 t_min = (box_mins - ray_start) / ray_dir;
    vec3 t_max = (box_maxs - ray_start) / ray_dir;
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

    // convert normal back into world space
    collision.n = transpose(inverse(mat3(model_rotation))) * collision.n;

    // calculate the world position of the hit point
    collision.p = ray.start + collision.t * ray.dir;

    // Texture coordinate

    // collision point in local space
    vec3 collisionPoint = (world_to_local_matrix * vec4(collision.p, 1.0)).xyz;

    // compute largest box dimension
    float totalWidth = box_maxs.x - box_mins.x;
    float totalHeight = box_maxs.y - box_mins.y;
    float totalDepth = box_maxs.z - box_mins.z;
    float maxDimesion = max(totalWidth, max(totalHeight, totalDepth));

    // convert X/Y/Z coordinate to range [0, 1], and devide by largest box dimension
    float rayStrikeX = (collisionPoint.x + totalWidth / 2.0) / maxDimesion;
    float rayStrikeY = (collisionPoint.y + totalHeight / 2.0) / maxDimesion;
    float rayStrikeZ = (collisionPoint.z + totalDepth / 2.0) / maxDimesion;

    // select (X,Y) / (X,Z) / (Y,Z) as texture coordinate depeending on box face
    switch(face_index) {
        case 0: collision.tc = vec2(rayStrikeZ, rayStrikeY);
        case 1: collision.tc = vec2(rayStrikeZ, rayStrikeX);
        default: collision.tc = vec2(rayStrikeY, rayStrikeX);
    }

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

    // Texture coordinate
    collision.tc.x = 0.5 + atan(-collision.n.z, collision.n.x) / (2.0 * PI);
    collision.tc.y = 0.5 - asin(-collision.n.y) / PI;

    return collision;
}



/*
Returns the closet collision of the ray
object_index == -1 -> no collision
object_index == 1 -> collision with sphere
object_index == 2 -> collision with box
*/

Collision get_closest_collision(Ray ray) {
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


vec3 adsLighting(Ray ray, Collision collision) {
    vec4 ambient = global_ambient + light_ambient * material_ambient;
    vec4 diffuse = vec4(0.0);
    vec4 specular = vec4(0.0);

    // Check if is in shadow
    Ray lightRay;
    lightRay.start = collision.p + collision.n * 0.01;//small offset along normal. Without it, it may bump into the object itself.
    lightRay.dir = normalize(light_position - collision.p);
    bool isInShadow = false;

    // Cast the ray against the scene
    Collision collision_shadow = get_closest_collision(lightRay);

    // if ray hit an object && hit between surface and light's position
    if (collision_shadow.object_index != -1 && collision_shadow.t < length(light_position - collision.p)) {
        isInShadow = true;
    }

    vec3 lightDir = normalize(light_position - collision.p);
    vec3 lightReflect = normalize(reflect(-lightDir, collision.n));
    float cosTheta = dot(lightDir, collision.n);
    float cosPhi = dot(normalize(-ray.dir), lightReflect);

    if (!isInShadow) {
        diffuse = light_diffuse * material_diffuse * max(cosTheta, 0.0);
        specular = light_specular * material_specular * pow(max(cosPhi, 0.0), material_shininess);
    }
    return (ambient + diffuse + specular).rgb;
}

vec3 raytrace(Ray ray) {
    Collision collision = get_closest_collision(ray);
    // TODO use switch statement
    if (collision.object_index == -1) { // no collision
        return vec3(0.0);// black
    }
    if (collision.object_index == 1) {
        return adsLighting(ray, collision) * texture(earthTexture, collision.tc).xyz;
    }
    if (collision.object_index == 2) {
        return adsLighting(ray, collision) * texture(brickTexture, collision.tc).xyz;
    }

    return vec3(1.0, 1.0, 1.0);// error white
}

void main() {
    int width = int(gl_NumWorkGroups.x);
    int height = int(gl_NumWorkGroups.y);
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);

    // conver this screen space location to world space
    float x_pixel = 2.0 * pixel.x / height - float(width) / float(height);
    float y_pixel = 2.0 * pixel.y / height - 1.0;

    // get this pixel's world-space ray
    Ray world_ray;
    world_ray.start = vec3(0.0, 0.0, camera_pos_z);
    vec4 world_ray_end = vec4(x_pixel, y_pixel, camera_pos_z - 1.0, 1.0);
    world_ray.dir = normalize(world_ray_end.xyz - world_ray.start);

    vec3 color = raytrace(world_ray);
    imageStore(output_texture, pixel, vec4(color, 1.0));
}
