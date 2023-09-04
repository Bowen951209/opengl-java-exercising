#version 430
layout(local_size_x = 1) in;
layout(binding = 0, rgba8) uniform image2D output_texture;
layout(binding = 1) uniform sampler2D boxTexture;

layout (binding=2) uniform sampler2D xpTex;
layout (binding=3) uniform sampler2D xnTex;
layout (binding=4) uniform sampler2D ypTex;
layout (binding=5) uniform sampler2D ynTex;
layout (binding=6) uniform sampler2D zpTex;
layout (binding=7) uniform sampler2D znTex;

uniform float camera_pos_x;
uniform float camera_pos_y;
uniform float camera_pos_z;

layout(binding=0) buffer inputRayStart {
    float[] input_ray_start;
};
layout(binding=1) buffer inputRayDir {
    float[] input_ray_dir;
};
layout(binding=2) buffer inputPixelList {
    int[] pixelList;
};

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
    vec2 tc;// texture coordinate of the collision point
    int face_index;// which box face (for room or sky box)
};

const int OBJ_TYPE_SKYBOX = 0, OBJ_TYPE_SPHERE = 1, OBJ_TYPE_BOX = 2, OBJ_TYPE_PLANE = 3;
struct Object {
    int type;// what type is the object? skybox/sphere/box/plane
    float radius;// for OBJ_TYPE_SPHERE
    vec3 mins;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE(x and z value are for width and height)
    vec3 maxs;// for OBJ_TYPE_BOX
    float xRotation;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE
    float yRotation;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE
    float zRotation;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE
    vec3 position;
    bool hasColor;
    bool hasTexture;
    bool isReflective;// whether the object is reflective. For type roombox, this is for enable/disable lighting.
    bool isTransparent;
    vec3 color;// for hasColor
    float refelctivity;// for isReflective
    float refractivity;// for isTransparent
    float IOR;// for is transparent
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

const float PI = 3.1415926535897932384626433832795;
const float DEG_TO_RAD = PI / 180.0;

// Defining Models
// Plane
vec3 plane_pos = vec3(0, -2.5, -2.0);//0,-1.2,-2
float plane_width = 12.0;
float plane_depth = 12.0;
float plane_xrot = DEG_TO_RAD * 0.0;
float plane_yrot = DEG_TO_RAD * 45.0;
float plane_zrot = DEG_TO_RAD * 0.0;

// Sphere
const float sphere_radius = 2.5;
const vec3 sphere_position = vec3(0.5, 0.0, -3.0);
const vec3 sphere_color = vec3(0.0, 0.0, 1.0);// blue

// Skybox
const float sbox_side_length = 40;
vec3 sbox_mins;
vec3 sbox_maxs;

// Room
vec3 rbox_color = vec3(1.0f, 0.5f, 0.5f);

// Box
const vec3 box_mins = vec3(-.5, -.5, -1.0);// a corner of the box
const vec3 box_maxs = vec3(.5, .5, 1.0);// a corner of the box
const vec3 box_color = vec3(1.0, 0.0, 0.0);// red
uniform vec3 box_position;
uniform vec3 box_rotation;

// Light
const vec4 global_ambient = vec4(.3, .3, .3, 1.0);
const vec4 material_ambient = vec4(.2, .2, .2, 1.0);
const vec4 material_diffuse = vec4(.7, .7, .7, 1.0);
const vec4 material_specular = vec4(1.0, 1.0, 1.0, 1.0);
const float material_shininess = 50.0;
uniform vec3 light_position;
const vec4 light_ambient = vec4(.2, .2, .2, 1.0);
const vec4 light_diffuse = vec4(.7, .7, .7, 1.0);
const vec4 light_specular = vec4(1.0, 1.0, 1.0, 1.0);

struct Stack_Element
{ int type;// The type of ray ( 1 = reflected, 2 = refracted )
    int depth;// The depth of the recursive raytrace
    int phase;// Keeps track of what phase each recursive call is at (each call is broken down into five phases)
    vec3 phong_color;// Contains the Phong ADS model color
    vec3 reflected_color;// Contains the reflected color
    vec3 refracted_color;// Contains the refracted color
    vec3 final_color;// Contains the final mixed output of the recursive raytrace call
    Ray ray;// The ray for this raytrace invocation
    Collision collision;// The collision for this raytrace invocation. Contains null_collision until phase 1
};

const int RAY_TYPE_REFLECTION = 1;
const int RAY_TYPE_REFRACTION = 2;

Ray null_ray = { vec3(0.0), vec3(0.0) };
Collision null_collision = { -1.0, vec3(0.0), vec3(0.0), false, -1, vec2(0.0, 0.0), -1 };
Stack_Element null_stack_element = { 0, -1, -1, vec3(0), vec3(0), vec3(0), vec3(0), null_ray, null_collision };

const int stack_size = 100;
Stack_Element stack[stack_size];
const int max_depth = 4;

int stack_pointer = -1;// Points to the top of the stack (-1 if empty)
Stack_Element popped_stack_element;// Holds the last popped element from the stack

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


Collision intersect_plane_object(Ray r) {
    // Compute the planes's local-space to world-space transform matrices, and their inverse
    mat4 local_to_worldT = buildTranslate(plane_pos);
    mat4 local_to_worldR = buildRotateY(plane_yrot) * buildRotateX(plane_xrot) * buildRotateZ(plane_zrot);
    mat4 local_to_worldTR = local_to_worldT * local_to_worldR;
    mat4 world_to_localTR = inverse(local_to_worldTR);
    mat4 world_to_localR = inverse(local_to_worldR);

    // Convert the world-space ray to the planes's local space:
    vec3 ray_start = (world_to_localTR * vec4(r.start, 1.0)).xyz;
    vec3 ray_dir = (world_to_localR * vec4(r.dir, 1.0)).xyz;

    Collision c;
    c.isInside = false;// there is no "inside" of a plane

    // compute intersection point of ray with plane
    c.t = dot((vec3(0, 0, 0) - ray_start), vec3(0, 1, 0)) / dot(ray_dir, vec3(0, 1, 0));

    // Calculate the world-position of the intersection:
    c.p = r.start + c.t * r.dir;

    // Calculate the position of the intersection in plane space:
    vec3 intersectPoint = ray_start + c.t * ray_dir;

    // If the ray didn't intersect the plane object, return a negative t value
    if ((abs(intersectPoint.x) > (plane_width/2.0)) || (abs(intersectPoint.z) > (plane_depth/2.0)))
    { c.t = -1.0;
        return c;
    }

    // Create the collision normal
    c.n = vec3(0.0, 1.0, 0.0);

    // If we hit the plane from the negative axis, invert the normal
    if (ray_dir.y > 0.0) c.n *= -1.0;

    // now convert the normal back into world space
    c.n = transpose(inverse(mat3(local_to_worldR))) * c.n;

    // Compute texture coordinates
    float maxDimension = max(plane_width, plane_depth);
    c.tc.x = (intersectPoint.x + plane_width/2.0)/maxDimension;
    c.tc.y = (intersectPoint.z + plane_depth/2.0)/maxDimension;
    return c;
}

Collision intersect_sky_box_object(Ray r)
{ // Calculate the box's world mins and maxs:
    vec3 t_min = (sbox_mins - r.start) / r.dir;
    vec3 t_max = (sbox_maxs - r.start) / r.dir;
    vec3 t_minDist = min(t_min, t_max);
    vec3 t_maxDist = max(t_min, t_max);
    float t_near = max(max(t_minDist.x, t_minDist.y), t_minDist.z);
    float t_far = min(min(t_maxDist.x, t_maxDist.y), t_maxDist.z);

    Collision c;
    c.t = t_near;
    c.isInside = false;

    // If the ray is entering the box, t_near contains the farthest boundary of entry
    // If the ray is leaving the box, t_far contains the closest boundary of exit
    // The ray intersects the box if and only if t_near < t_far, and if t_far > 0.0

    // If the ray didn't intersect the box, return a negative t value
    if (t_near >= t_far || t_far <= 0.0)
    { c.t = -1.0;
        return c;
    }

    float intersection = t_near;
    vec3 boundary = t_minDist;

    // if t_near < 0, then the ray started inside the box and left the box
    if (t_near < 0.0)
    { c.t = t_far;
        intersection = t_far;
        boundary = t_maxDist;
        c.isInside = true;
    }

    // Checking which boundary the intersection lies on
    int face_index = 0;
    if (intersection == boundary.y) face_index = 1;
    else if (intersection == boundary.z) face_index = 2;

    // Creating the collision normal
    c.n = vec3(0.0);
    c.n[face_index] = 1.0;

    // If we hit the box from the negative axis, invert the normal
    if (r.dir[face_index] > 0.0) c.n *= -1.0;

    // Calculate the world-position of the intersection:
    c.p = r.start + c.t * r.dir;

    // Calculate face index for collision object
    if (c.n == vec3(1, 0, 0)) c.face_index = 0;
    else if (c.n == vec3(-1, 0, 0)) c.face_index = 1;
    else if (c.n == vec3(0, 1, 0)) c.face_index = 2;
    else if (c.n == vec3(0, -1, 0)) c.face_index = 3;
    else if (c.n == vec3(0, 0, 1)) c.face_index = 4;
    else if (c.n == vec3(0, 0, -1)) c.face_index = 5;

    // Compute texture coordinates
    // compute largest box dimension
    float totalWidth = sbox_maxs.x - sbox_mins.x;
    float totalHeight = sbox_maxs.y - sbox_mins.y;
    float totalDepth = sbox_maxs.z - sbox_mins.z;
    float maxDimension = max(totalWidth, max(totalHeight, totalDepth));

    // select tex coordinates depending on box face
    float rayStrikeX = ((c.p).x  - camera_pos_x + totalWidth/2.0)/maxDimension;
    float rayStrikeY = ((c.p).y  - camera_pos_y+ totalHeight/2.0)/maxDimension;
    float rayStrikeZ = ((c.p).z - camera_pos_z + totalDepth/2.0)/maxDimension;

    if (c.face_index == 0)// xn
    c.tc = vec2(rayStrikeZ, -rayStrikeY);
    else if (c.face_index == 1)// xp
    c.tc = vec2(1.0-rayStrikeZ, -rayStrikeY);
    else if (c.face_index == 2)// yn
    c.tc = vec2(rayStrikeX, -rayStrikeZ);
    else if (c.face_index == 3)// yp
    c.tc = vec2(rayStrikeX, rayStrikeZ - 1.0);
    else if (c.face_index == 4)// zn
    c.tc = vec2(1.0-rayStrikeX, -rayStrikeY);
    else if (c.face_index == 5)// zp
    c.tc = vec2(rayStrikeX, -rayStrikeY);

    return c;
}

// ----------------------------Check if the ray hit the box----------------------------
Collision intersect_box_object(Ray ray) {
    mat4 model_translation = buildTranslate(box_position);
    mat4 model_rotation = buildRotation(
    box_rotation.x * DEG_TO_RAD,
    box_rotation.y * DEG_TO_RAD,
    box_rotation.z * DEG_TO_RAD
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
    if (ray_dir[face_index] > 0.0) {
        collision.n *= -1.0;
    }

    // convert normal back into world space
    mat4 invTrMatrix = transpose(world_to_local_rotation_matrix);
    collision.n = mat3(invTrMatrix) * collision.n;

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

    if (face_index == 0){
        collision.tc = vec2(rayStrikeZ, rayStrikeY);
    }
    else if (face_index == 1){
        collision.tc = vec2(rayStrikeZ, rayStrikeX);
    }
    else {
        collision.tc = vec2(rayStrikeY, rayStrikeX);
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
    Collision closest_collision, cSphere, cBox, cSBox, cPlane;
    closest_collision.object_index = -1;

    cSphere = intersect_sphere_object(ray);
    cBox = intersect_box_object(ray);
    cSBox = intersect_sky_box_object(ray);
    cPlane = intersect_plane_object(ray);

    if ((cSphere.t > 0) && ((cSphere.t < cBox.t) || (cBox.t < 0))) {
        closest_collision = cSphere;
        closest_collision.object_index = 1;
    }
    if ((cBox.t > 0) && ((cBox.t < cSphere.t) || (cSphere.t < 0))) {
        closest_collision = cBox;
        closest_collision.object_index = 2;
    }
    if ((cSBox.t > 0) && ((cSBox.t < cSphere.t) || (cSphere.t < 0)) && ((cSBox.t < cBox.t) || (cBox.t < 0)))
    { closest_collision = cSBox;
        closest_collision.object_index = 3;
    }
    if ((cPlane.t > 0) &&
    ((cPlane.t < cSphere.t) || (cSphere.t < 0))
    && ((cPlane.t < cBox.t) || (cBox.t < 0))
    && ((cPlane.t < cSBox.t) || (cSBox.t < 0)))
    { closest_collision = cPlane;
        closest_collision.object_index = 4;
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

vec3 checkerboard(vec2 tc)
{ float tileScale = 24.0;
    float tile = mod(floor(tc.x * tileScale) + floor(tc.y * tileScale), 2.0);
    return tile * vec3(1, 1, 1);
}


//------------------------------------------------------------------------------
// Schedules a new raytrace by adding it to the top of the stack
//------------------------------------------------------------------------------
void push(Ray r, int depth, int type)
{ if (stack_pointer >= stack_size-1)  return;

    Stack_Element element;
    element = null_stack_element;
    element.type = type;
    element.depth = depth;
    element.phase = 1;
    element.ray = r;

    stack_pointer++;
    stack[stack_pointer] = element;
}

//------------------------------------------------------------------------------
// Removes the topmost stack element
//------------------------------------------------------------------------------
Stack_Element pop()
{ // Store the element we're removing in top_stack_element
    Stack_Element top_stack_element = stack[stack_pointer];

    // Erase the element from the stack
    stack[stack_pointer] = null_stack_element;
    stack_pointer--;
    return top_stack_element;
}

//------------------------------------------------------------------------------
// This function processes the stack element at a given index
// This function is guaranteed to be ran on the topmost stack element
//------------------------------------------------------------------------------
void process_stack_element(int index)
{
    // If there is a popped_stack_element that just ran, it holds one of our values
    // Store it and delete it
    if (popped_stack_element != null_stack_element)
    { if (popped_stack_element.type == RAY_TYPE_REFLECTION)
    stack[index].reflected_color = popped_stack_element.final_color;
    else if (popped_stack_element.type == RAY_TYPE_REFRACTION)
    stack[index].refracted_color = popped_stack_element.final_color;
        popped_stack_element = null_stack_element;
    }

    Ray r = stack[index].ray;
    Collision c = stack[index].collision;

    // Iterate through the raytrace phases (explained below)
    switch (stack[index].phase)
    { //=================================================
        // PHASE 1 - Raytrace Collision Detection
        //=================================================
        case 1:
        c = get_closest_collision(r);// Cast ray against the scene, store the collision result
        if (c.object_index != -1)// If the ray didn't hit anything, stop.
        stack[index].collision = c;// otherwise, store the collision result
        break;
        //=================================================
        // PHASE 2 - Phong ADS Lighting Computation
        //=================================================
        case 2:
        stack[index].phong_color = adsLighting(r, c);
        break;
        //=================================================
        // PHASE 3 - Reflection Bounce Pass Computation
        //=================================================
        case 3:
        // Only make recursive raytrace passes if we're not at max depth
        if (stack[index].depth < max_depth)
        { // only the sphere and box are reflective
            if ((c.object_index == 1) || (c.object_index == 2))
            { Ray reflected_ray;
                reflected_ray.start = c.p + c.n * 0.001;
                reflected_ray.dir = reflect(r.dir, c.n);

                // Add a raytrace for that ray to the stack
                push(reflected_ray, stack[index].depth+1, RAY_TYPE_REFLECTION);
            } }
        break;
        //=================================================
        // PHASE 4 - Refraction Transparency Pass Computation
        //=================================================
        case 4:
        // Only make recursive raytrace passes if we're not at max depth
        if (stack[index].depth < max_depth)
        { // only the sphere is transparent
            if (c.object_index == 1)
            { Ray refracted_ray;
                refracted_ray.start = c.p - c.n * 0.001;
                float refraction_ratio = 0.66667;
                if (c.isInside) refraction_ratio = 1.0 / refraction_ratio;
                refracted_ray.dir = refract(r.dir, c.n, refraction_ratio);

                // Add a raytrace for that ray to the stack
                push(refracted_ray, stack[index].depth+1, RAY_TYPE_REFRACTION);
            } }
        break;
        //=================================================
        // PHASE 5 - Mixing to produce the final color
        //=================================================
        case 5:
        if (c.object_index == 1)
        { stack[index].final_color = stack[index].phong_color *
        ((0.3 * stack[index].reflected_color) + (2.0 * stack[index].refracted_color));
        }
        if (c.object_index == 2)
        { stack[index].final_color = stack[index].phong_color *
        ((0.5 * stack[index].reflected_color) + (1.0 * (texture(boxTexture, c.tc)).rgb));
        }
        if (c.object_index == 3) stack[index].final_color = stack[index].phong_color * rbox_color;
        if (c.object_index == 4) stack[index].final_color = stack[index].phong_color * (checkerboard(c.tc)).xyz;
        break;
        //=================================================
        // when all five phases are complete, end the recursion
        //=================================================
        case 6: { popped_stack_element = pop(); return; }
    }
    stack[index].phase++;
    return;// Only process one phase per process_stack_element() invocation
}

vec3 raytrace(Ray r){
    // Add a raytrace to the stack
    push(r, 0, RAY_TYPE_REFLECTION);

    // Process the stack until it's empty
    while (stack_pointer >= 0)
    { int element_index = stack_pointer;// Peek at the topmost stack element
        process_stack_element(element_index);// Process this stack element
    }

    // Return the final_color value of the last-popped stack element
    return popped_stack_element.final_color;
}

void calcSkyboxCorners() {
    float sbox_side_length_d2 = sbox_side_length / 2.0;
    sbox_mins = vec3(-sbox_side_length_d2) + vec3(camera_pos_x, camera_pos_y, camera_pos_z);
    sbox_maxs = vec3(sbox_side_length_d2) + vec3(camera_pos_x, camera_pos_y, camera_pos_z);
}

const int STATE_NO_DRAW = 0;
const int STATE_DO_DRAW = 1;
const int STATE_DRAWN = 2;

bool shouldRender(uint index) {
    if (pixelList[index] == STATE_DO_DRAW) {
        return true;
    } else {
        return false;
    }
}

void main() {
    int width = int(gl_NumWorkGroups.x);
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    uint pixelIndex = gl_GlobalInvocationID.x + gl_GlobalInvocationID.y * width;


    if (shouldRender(pixelIndex)){
        calcSkyboxCorners();
        Ray world_ray;
        uint rayInfoIndex = pixelIndex * 3;

        if (shouldRender(rayInfoIndex / 3))
        world_ray.start.x = input_ray_start[rayInfoIndex];
        world_ray.start.y = input_ray_start[rayInfoIndex + 1];
        world_ray.start.z = input_ray_start[rayInfoIndex + 2];

        world_ray.dir.x = input_ray_dir[rayInfoIndex];
        world_ray.dir.y = input_ray_dir[rayInfoIndex + 1];
        world_ray.dir.z = input_ray_dir[rayInfoIndex + 2];
        // ---------------------------------------------

        vec3 color = raytrace(world_ray);
        imageStore(output_texture, pixel, vec4(color, 1.0));
        pixelList[pixelIndex] = STATE_DRAWN;
    }
}
