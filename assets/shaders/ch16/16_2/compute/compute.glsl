#version 430

layout(local_size_x = 1) in;

// Texture & Image
layout(binding = 0, rgba8) uniform image2D outputTexture;
layout(binding = 1) uniform sampler2D boxTexture;

// The input ray info from ray shader
layout(binding=0) buffer inputRayStart {
    float[] rayStart;
};
layout(binding=1) buffer inputRayDir {
    float[] rayDir;
};
layout(binding=2) buffer inputPixelList {
    int[] pixelList;
};

// Structures
struct Ray {
    vec3 start;// origin
    vec3 dir;// normalized direction
};

struct Collision {
    float t;// distance from ray's origin to collision point
    vec3 p;// world position
    vec3 n;// normal at the collision point
    bool isInside;// whether ray started inside an object and collided
    int objectIndex;// index of the object that the ray hits
    vec2 tc;// texture coordinate of the collision point
    int faceIndex;// which box face (for room or sky box)
};

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

struct StackElement{
    int type;// The type of ray ( 1 = reflected, 2 = refracted )
    int depth;// The depth of the recursive raytrace
    int phase;// Keeps track of what phase each recursive call is at (each call is broken down into five phases)
    vec3 lightingColor;// Contains the Phong ADS model color
    vec3 reflectedColor;// Contains the reflected color
    vec3 refractedColor;// Contains the refracted color
    vec3 finalColor;// Contains the final mixed output of the recursive raytrace call
    Ray ray;// The ray for this raytrace invocation
    Collision collision;// The collision for this raytrace invocation. Contains null_collision until phase 1
};

// Constants
const int STATE_NO_DRAW = 0;
const int STATE_DO_DRAW = 1;
const int STATE_DRAWN = 2;
const int OBJ_TYPE_SKYBOX = 0, OBJ_TYPE_SPHERE = 1, OBJ_TYPE_BOX = 2, OBJ_TYPE_PLANE = 3;
const float PI = 3.1415926535897932384626433832795;
const float DEG_TO_RAD = PI / 180.0;
const int RAY_TYPE_REFLECTION = 1, RAY_TYPE_REFRACTION = 2;
const Ray null_ray = {vec3(0.0), vec3(0.0)};
const Collision null_collision = {-1.0, vec3(0.0), vec3(0.0), false, -1, vec2(0.0, 0.0), -1};
const StackElement nullStackElement = {
    0, -1, -1, vec3(0.0), vec3(0.0), vec3(0.0), vec3(0.0), null_ray, null_collision
};
const int STACK_SIZE = 100;
const int RAY_MAX_DEPTH = 4;

const vec3 PLANE_POSITION = vec3(0, -2.5, -2.0);//0,-1.2,-2
const float PLANE_WIDTH = 12.0;
const float PLANE_DEPTH = 12.0;
const float PLANE_ROTATION_X = DEG_TO_RAD * 0.0;
const float PLANE_ROTATION_Y = DEG_TO_RAD * 45.0;
const float PLANE_ROTATION_Z = DEG_TO_RAD * 0.0;
const float SPHERE_RADIUS = 2.5;
const vec3 SPHERE_POSITION = vec3(0.5, 0.0, -3.0);
const float SKYBOX_SIDE_LENGTH = 40;
const vec3 BOX_MINS = vec3(-.5, -.5, -1.0);// a corner of the box
const vec3 BOX_MAXS = vec3(.5, .5, 1.0);// a corner of the box
const vec4 GLOBAL_AMBIENT = vec4(.3, .3, .3, 1.0);
const vec4 MATERIAL_AMBIENT = vec4(.2, .2, .2, 1.0);
const vec4 MATERIAL_DIFFUSE = vec4(.7, .7, .7, 1.0);
const vec4 MATERIAL_SPECULAR = vec4(1.0, 1.0, 1.0, 1.0);
const float MATERIAL_SHININESS = 50.0;
const vec4 LIGHT_AMBIENT = vec4(.2, .2, .2, 1.0);
const vec4 LIGHT_DIFFUSE = vec4(.7, .7, .7, 1.0);
const vec4 LIGHT_SPECULAR = vec4(1.0, 1.0, 1.0, 1.0);
const vec3 ROOOM_BOX_COLOR = vec3(1.0, .5, .5);


// Uniforms
uniform vec3 cameraPosition;
uniform vec3 boxPosition;
uniform vec3 boxRotation;
uniform vec3 lightPosition;

// Variables
StackElement stack[STACK_SIZE];
int stackPointer = -1;// Points to the top of the stack (-1 if empty)
StackElement poppedStackElement;// Holds the last popped element from the stack
vec3 skyboxMins;
vec3 skyboxMaxs;

Object[] objects = {
    // TODO: apply to my user settings
    // Roombox (can also be applied to skybox)
    {
        OBJ_TYPE_SKYBOX, .0, vec3(-20.0, -20.0, -20.0), vec3(20.0, 20.0, 20.0), .0, .0, .0, vec3(0.0), true, false,
        true, false, vec3(.25, 1.0, 1.0), .0, .0, .0, vec4(.2, .2, .2, 1.0), vec4(.9, .9, .9, 1.0),
        vec4(1.0, 1.0, 1.0, 1.0), 50.0
    },

    // Ground plane
    {
        OBJ_TYPE_PLANE, .0, vec3(12.0, .0, 16.0), vec3(.0), .0, .0, .0, vec3(.0, -1.0, -2.0), false, true, false, false,
        vec3(.0), .0, .0, .0, vec4(.2, .2, .2, 1.0), vec4(.9, .9, .9, 1.0), vec4(1.0), 50.0
    },

    // Transparent sphere with slight refection and no texture
    {
        OBJ_TYPE_SPHERE, 1.2, vec3(.0), vec3(.0), .0, .0, .0, vec3(.7, .2, 2.0), false, false, true, true, vec3(.0),
        .8, .8, 1.5, vec4(.5, .5, .5, 1.0), vec4(1.0), vec4(1.0), 50.0
    },

    // Slightlly reflective box with texture
    {
        OBJ_TYPE_BOX, .0, vec3(-.25, -.8, -.25), vec3(.25, .8, .25), .0, 70.0, .0, vec3(-.75, -.2, 3.4), false, true,
        true, false, vec3(.0), .5, .0, .0, vec4(.5, .5, .5, 1.0), vec4(1.0, 1.0, 1.0, 1.0), vec4(1.0, 1.0, 1.0, 1.0),
        50.0
    }

};

// Useful Methods
// TODO: I should build these matrxi just once in the CPU, not for every pixel in GPU
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

Collision intersectPlaneObject(Ray r) {
    // Compute the planes's local-space to world-space transform matrices, and their inverse
    mat4 localToWorldT = buildTranslate(PLANE_POSITION);
    mat4 localToWorldR = buildRotation(PLANE_ROTATION_X, PLANE_ROTATION_Y, PLANE_ROTATION_Z);
    mat4 localToWorldTR = localToWorldT * localToWorldR;
    mat4 worldToLocalTR = inverse(localToWorldTR);
    mat4 worldToLocalR = inverse(localToWorldR);

    // Convert the world-space ray to the planes's local space:
    vec3 rayStart = (worldToLocalTR * vec4(r.start, 1.0)).xyz;
    vec3 rayDir = (worldToLocalR * vec4(r.dir, 1.0)).xyz;

    Collision c;
    c.isInside = false;// there is no "inside" of a plane

    // compute intersection point of ray with plane
    c.t = dot((vec3(0, 0, 0) - rayStart), vec3(0, 1, 0)) / dot(rayDir, vec3(0, 1, 0));

    // Calculate the world-position of the intersection:
    c.p = r.start + c.t * r.dir;

    // Calculate the position of the intersection in plane space:
    vec3 intersectPoint = rayStart + c.t * rayDir;

    // If the ray didn't intersect the plane object, return a negative t value
    if ((abs(intersectPoint.x) > (PLANE_WIDTH/2.0)) || (abs(intersectPoint.z) > (PLANE_DEPTH/2.0))) {
        c.t = -1.0;
        return c;
    }

    // Create the collision normal
    c.n = vec3(0.0, 1.0, 0.0);

    // If we hit the plane from the negative axis, invert the normal
    if (rayDir.y > 0.0) c.n *= -1.0;

    // now convert the normal back into world space
    c.n = transpose(inverse(mat3(localToWorldR))) * c.n;

    // Compute texture coordinates
    float maxDimension = max(PLANE_WIDTH, PLANE_DEPTH);
    c.tc.x = (intersectPoint.x + PLANE_WIDTH/2.0)/maxDimension;
    c.tc.y = (intersectPoint.z + PLANE_DEPTH/2.0)/maxDimension;
    return c;
}

Collision intersectSkyboxObject(Ray r){
    // Calculate the box's world mins and maxs:
    vec3 tMin = (skyboxMins - r.start) / r.dir;
    vec3 tMax = (skyboxMaxs - r.start) / r.dir;
    vec3 tMinDist = min(tMin, tMax);
    vec3 tMaxDist = max(tMin, tMax);
    float tNear = max(max(tMinDist.x, tMinDist.y), tMinDist.z);
    float tFar = min(min(tMaxDist.x, tMaxDist.y), tMaxDist.z);

    Collision c;
    c.t = tNear;
    c.isInside = false;

    // If the ray is entering the box, tNear contains the farthest boundary of entry
    // If the ray is leaving the box, tFar contains the closest boundary of exit
    // The ray intersects the box if and only if tNear < tFar, and if tFar > 0.0

    // If the ray didn't intersect the box, return a negative t value
    if (tNear >= tFar || tFar <= 0.0) {
        c.t = -1.0;
        return c;
    }

    float intersection = tNear;
    vec3 boundary = tMinDist;

    // if tNear < 0, then the ray started inside the box and left the box
    if (tNear < 0.0) {
        c.t = tFar;
        intersection = tFar;
        boundary = tMaxDist;
        c.isInside = true;
    }

    // Checking which boundary the intersection lies on
    int faceIndex = 0;
    if (intersection == boundary.y) faceIndex = 1;
    else if (intersection == boundary.z) faceIndex = 2;

    // Creating the collision normal
    c.n = vec3(0.0);
    c.n[faceIndex] = 1.0;

    // If we hit the box from the negative axis, invert the normal
    if (r.dir[faceIndex] > 0.0) c.n *= -1.0;

    // Calculate the world-position of the intersection:
    c.p = r.start + c.t * r.dir;

    // Calculate face index for collision object
    if (c.n == vec3(1, 0, 0)) c.faceIndex = 0;
    else if (c.n == vec3(-1, 0, 0)) c.faceIndex = 1;
    else if (c.n == vec3(0, 1, 0)) c.faceIndex = 2;
    else if (c.n == vec3(0, -1, 0)) c.faceIndex = 3;
    else if (c.n == vec3(0, 0, 1)) c.faceIndex = 4;
    else if (c.n == vec3(0, 0, -1)) c.faceIndex = 5;

    // Compute texture coordinates
    // compute largest box dimension
    float totalWidth = skyboxMaxs.x - skyboxMins.x;
    float totalHeight = skyboxMaxs.y - skyboxMins.y;
    float totalDepth = skyboxMaxs.z - skyboxMins.z;
    float maxDimension = max(totalWidth, max(totalHeight, totalDepth));

    // select tex coordinates depending on box face
    float rayStrikeX = ((c.p).x  - cameraPosition.x + totalWidth/2.0)/maxDimension;
    float rayStrikeY = ((c.p).y  - cameraPosition.y+ totalHeight/2.0)/maxDimension;
    float rayStrikeZ = ((c.p).z - cameraPosition.z + totalDepth/2.0)/maxDimension;

    if (c.faceIndex == 0)// xn
    c.tc = vec2(rayStrikeZ, -rayStrikeY);
    else if (c.faceIndex == 1)// xp
    c.tc = vec2(1.0-rayStrikeZ, -rayStrikeY);
    else if (c.faceIndex == 2)// yn
    c.tc = vec2(rayStrikeX, -rayStrikeZ);
    else if (c.faceIndex == 3)// yp
    c.tc = vec2(rayStrikeX, rayStrikeZ - 1.0);
    else if (c.faceIndex == 4)// zn
    c.tc = vec2(1.0-rayStrikeX, -rayStrikeY);
    else if (c.faceIndex == 5)// zp
    c.tc = vec2(rayStrikeX, -rayStrikeY);

    return c;
}

Collision intersectBoxObject(Ray ray) {
    mat4 modelTranslation = buildTranslate(boxPosition);
    mat4 modelRotation = buildRotation(
    boxRotation.x * DEG_TO_RAD,
    boxRotation.y * DEG_TO_RAD,
    boxRotation.z * DEG_TO_RAD
    );

    mat4 localToWorldMatrix = modelTranslation * modelRotation;
    mat4 worldToLocalMatrix = inverse(localToWorldMatrix);
    mat4 worldToLocalRotationMatrix = inverse(modelRotation);

    vec3 rayStart = (worldToLocalMatrix * vec4(ray.start, 1.0)).xyz;
    vec3 rayDir = (worldToLocalRotationMatrix * vec4(ray.dir, 1.0)).xyz;


    // calculate the box's mins and maxs
    vec3 tMin = (BOX_MINS - rayStart) / rayDir;
    vec3 tMax = (BOX_MAXS - rayStart) / rayDir;
    vec3 tMinDist = min(tMin, tMax);
    vec3 tMaxDist = max(tMin, tMax);
    float tNear = max(max(tMinDist.x, tMinDist.y), tMinDist.z);
    float tFar = min(min(tMaxDist.x, tMaxDist.y), tMaxDist.z);

    Collision collision;
    collision.t = tNear;
    collision.isInside = false;

    // if the ray didn't hit the box, return a negative t value
    if (tNear > tFar || tFar < 0.0) {
        collision.t = -1.0;
        return collision;
    }

    float intersectDistance = tNear;
    vec3 planeIntersectDistances = tMinDist;

    // if tNear < 0, then the ray started inside the box and left
    if (tNear < 0.0) {
        collision.t = tFar;
        intersectDistance = tFar;
        planeIntersectDistances = tMaxDist;
        collision.isInside = true;
    }

    // check which boundary the ray hits
    int faceIndex = 0;
    if (intersectDistance == planeIntersectDistances.y) {
        faceIndex = 1;
    } else if (intersectDistance == planeIntersectDistances.z) {
        faceIndex = 2;
    }

    // create collision normal
    collision.n = vec3(0.0);
    collision.n[faceIndex] = 1.0;
    // if hit the box from the negative axis, invert the normal
    if (rayDir[faceIndex] > 0.0) {
        collision.n *= -1.0;
    }

    // convert normal back into world space
    mat4 invTrMatrix = transpose(worldToLocalRotationMatrix);
    collision.n = mat3(invTrMatrix) * collision.n;

    // calculate the world position of the hit point
    collision.p = ray.start + collision.t * ray.dir;

    // Texture coordinate

    // collision point in local space
    vec3 collisionPoint = (worldToLocalMatrix * vec4(collision.p, 1.0)).xyz;

    // compute largest box dimension
    float totalWidth = BOX_MAXS.x - BOX_MINS.x;
    float totalHeight = BOX_MAXS.y - BOX_MINS.y;
    float totalDepth = BOX_MAXS.z - BOX_MINS.z;
    float maxDimesion = max(totalWidth, max(totalHeight, totalDepth));

    // convert X/Y/Z coordinate to range [0, 1], and devide by largest box dimension
    float rayStrikeX = (collisionPoint.x + totalWidth / 2.0) / maxDimesion;
    float rayStrikeY = (collisionPoint.y + totalHeight / 2.0) / maxDimesion;
    float rayStrikeZ = (collisionPoint.z + totalDepth / 2.0) / maxDimesion;

    // select (X,Y) / (X,Z) / (Y,Z) as texture coordinate depeending on box face

    if (faceIndex == 0){
        collision.tc = vec2(rayStrikeZ, rayStrikeY);
    }
    else if (faceIndex == 1){
        collision.tc = vec2(rayStrikeZ, rayStrikeX);
    }
    else {
        collision.tc = vec2(rayStrikeY, rayStrikeX);
    }

    return collision;
}

Collision intersectSphereObject(Ray ray) {
    float qa = dot(ray.dir, ray.dir);
    float qb = dot(2.0 * ray.dir, ray.start - SPHERE_POSITION);
    float qc = dot(ray.start - SPHERE_POSITION, ray.start - SPHERE_POSITION)
    - SPHERE_RADIUS * SPHERE_RADIUS;

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
    float tNear = min(t1, t2);
    float tFar = max(t1, t2);
    collision.t = tNear;

    if (tFar < 0.0) { // sphere is behind the ray, no inersection
        collision.t = -1.0;
        return collision;
    }

    if (tNear < 0.0) { // ray started inside the sphere
        collision.t = tFar;
        collision.isInside = true;
    }

    collision.p = ray.start + collision.t * ray.dir;// world position if the hit point
    collision.n = normalize(collision.p - SPHERE_POSITION);

    if (collision.isInside) { // if ray is inside, flip the normal
        collision.n *= -1.0;
    }

    // Texture coordinate
    collision.tc.x = 0.5 + atan(-collision.n.z, collision.n.x) / (2.0 * PI);
    collision.tc.y = 0.5 - asin(-collision.n.y) / PI;

    return collision;
}

/**
 * Returns the closet collision of the ray
 * objectIndex == -1 -> no collision
 * objectIndex == 1 -> collision with sphere
 * objectIndex == 2 -> collision with box
 */
Collision getClosestCollision(Ray ray) {
    Collision closestCollision, cSphere, cBox, cSBox, cPlane;
    closestCollision.objectIndex = -1;

    cSphere = intersectSphereObject(ray);
    cBox = intersectBoxObject(ray);
    cSBox = intersectSkyboxObject(ray);
    cPlane = intersectPlaneObject(ray);

    if ((cSphere.t > 0) && ((cSphere.t < cBox.t) || (cBox.t < 0))) {
        closestCollision = cSphere;
        closestCollision.objectIndex = 1;
    }
    if ((cBox.t > 0) && ((cBox.t < cSphere.t) || (cSphere.t < 0))) {
        closestCollision = cBox;
        closestCollision.objectIndex = 2;
    }
    if ((cSBox.t > 0) && ((cSBox.t < cSphere.t) || (cSphere.t < 0)) && ((cSBox.t < cBox.t) || (cBox.t < 0)))
    { closestCollision = cSBox;
        closestCollision.objectIndex = 3;
    }
    if ((cPlane.t > 0) &&
    ((cPlane.t < cSphere.t) || (cSphere.t < 0))
    && ((cPlane.t < cBox.t) || (cBox.t < 0))
    && ((cPlane.t < cSBox.t) || (cSBox.t < 0)))
    { closestCollision = cPlane;
        closestCollision.objectIndex = 4;
    }

    return closestCollision;
}

vec3 adsLighting(Ray ray, Collision collision) {
    vec4 ambient = GLOBAL_AMBIENT + LIGHT_AMBIENT * MATERIAL_AMBIENT;
    vec4 diffuse = vec4(0.0);
    vec4 specular = vec4(0.0);

    // Check if is in shadow
    Ray lightRay;
    lightRay.start = collision.p + collision.n * 0.01;//small offset along normal. Without it, it may bump into the object itself.
    lightRay.dir = normalize(lightPosition - collision.p);
    bool isInShadow = false;

    // Cast the ray against the scene
    Collision collisionShadow = getClosestCollision(lightRay);

    // if ray hit an object && hit between surface and light's position
    if (collisionShadow.objectIndex != -1 && collisionShadow.t < length(lightPosition - collision.p)) {
        isInShadow = true;
    }

    vec3 lightDir = normalize(lightPosition - collision.p);
    vec3 lightReflect = normalize(reflect(-lightDir, collision.n));
    float cosTheta = dot(lightDir, collision.n);
    float cosPhi = dot(normalize(-ray.dir), lightReflect);

    if (!isInShadow) {
        diffuse = LIGHT_DIFFUSE * MATERIAL_DIFFUSE * max(cosTheta, 0.0);
        specular = LIGHT_SPECULAR * MATERIAL_SPECULAR * pow(max(cosPhi, 0.0), MATERIAL_SHININESS);
    }
    return (ambient + diffuse + specular).rgb;
}

vec3 checkerboard(vec2 tc) {
    float tileScale = 24.0;
    float tile = mod(floor(tc.x * tileScale) + floor(tc.y * tileScale), 2.0);
    return tile * vec3(1, 1, 1);
}

void calcSkyboxCorners() {
    float skyboxSideLengthD2 = SKYBOX_SIDE_LENGTH / 2.0;
    skyboxMins = vec3(-skyboxSideLengthD2) + vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z);
    skyboxMaxs = vec3(skyboxSideLengthD2) + vec3(cameraPosition.x, cameraPosition.y, cameraPosition.z);
}

bool shouldRender(uint index) {
    if (pixelList[index] == STATE_DO_DRAW) {
        return true;
    } else {
        return false;
    }
}

void push(Ray r, int depth, int type) {
    if (stackPointer >= STACK_SIZE-1)  return;

    StackElement element;
    element = nullStackElement;
    element.type = type;
    element.depth = depth;
    element.phase = 1;
    element.ray = r;

    stackPointer++;
    stack[stackPointer] = element;
}

StackElement pop() {
    // Store the element we're removing in topStackElement
    StackElement topStackElement = stack[stackPointer];

    // Erase the element from the stack
    stack[stackPointer] = nullStackElement;
    stackPointer--;
    return topStackElement;
}

void processStackElement(int index) {
    // If there is a poppedStackElement that just ran, it holds one of our values
    // Store it and delete it
    if (poppedStackElement != nullStackElement)
    { if (poppedStackElement.type == RAY_TYPE_REFLECTION)
    stack[index].reflectedColor = poppedStackElement.finalColor;
    else if (poppedStackElement.type == RAY_TYPE_REFRACTION)
    stack[index].refractedColor = poppedStackElement.finalColor;
        poppedStackElement = nullStackElement;
    }

    Ray r = stack[index].ray;
    Collision c = stack[index].collision;

    // Iterate through the raytrace phases (explained below)
    switch (stack[index].phase) {
        //=================================================
        // PHASE 1 - Raytrace Collision Detection
        //=================================================
        case 1:
        c = getClosestCollision(r);// Cast ray against the scene, store the collision result
        if (c.objectIndex != -1)// If the ray didn't hit anything, stop.
            stack[index].collision = c;// otherwise, store the collision result
        break;
        //=================================================
        // PHASE 2 - Phong ADS Lighting Computation
        //=================================================
        case 2:
        stack[index].lightingColor = adsLighting(r, c);
        break;
        //=================================================
        // PHASE 3 - Reflection Bounce Pass Computation
        //=================================================
        case 3:
        // Only make recursive raytrace passes if we're not at max depth
        if (stack[index].depth < RAY_MAX_DEPTH) {
            // only the sphere and box are reflective
            if ((c.objectIndex == 1) || (c.objectIndex == 2)) {
                Ray reflectedRay;
                reflectedRay.start = c.p + c.n * 0.001;
                reflectedRay.dir = reflect(r.dir, c.n);

                // Add a raytrace for that ray to the stack
                push(reflectedRay, stack[index].depth+1, RAY_TYPE_REFLECTION);
            }
        }
        break;
        //=================================================
        // PHASE 4 - Refraction Transparency Pass Computation
        //=================================================
        case 4:
        // Only make recursive raytrace passes if we're not at max depth
        if (stack[index].depth < RAY_MAX_DEPTH) {
            // only the sphere is transparent
            if (c.objectIndex == 1) {
                Ray refractedRay;
                refractedRay.start = c.p - c.n * 0.001;
                float refractionRatio = 0.66667;
                if (c.isInside) refractionRatio = 1.0 / refractionRatio;
                refractedRay.dir = refract(r.dir, c.n, refractionRatio);

                // Add a raytrace for that ray to the stack
                push(refractedRay, stack[index].depth+1, RAY_TYPE_REFRACTION);
            }
        }
        break;
        //=================================================
        // PHASE 5 - Mixing to produce the final color
        //=================================================
        case 5:
        if (c.objectIndex == 1) {
            stack[index].finalColor = stack[index].lightingColor *
                ((0.3 * stack[index].reflectedColor) + (2.0 * stack[index].refractedColor));
        }
        if (c.objectIndex == 2) {
            stack[index].finalColor = stack[index].lightingColor *
                ((0.5 * stack[index].reflectedColor) + (1.0 * (texture(boxTexture, c.tc)).rgb));
        }
        if (c.objectIndex == 3) stack[index].finalColor = stack[index].lightingColor * ROOOM_BOX_COLOR;
        if (c.objectIndex == 4) stack[index].finalColor = stack[index].lightingColor * (checkerboard(c.tc)).xyz;
        break;
        //=================================================
        // when all five phases are complete, end the recursion
        //=================================================
        case 6: { poppedStackElement = pop(); return; }
    }
    stack[index].phase++;
    return;// Only process one phase per processStackElement() invocation
}

vec3 raytrace(Ray r){
    // Add a raytrace to the stack
    push(r, 0, RAY_TYPE_REFLECTION);

    // Process the stack until it's empty
    while (stackPointer >= 0) {
        int elementIndex = stackPointer;// Peek at the topmost stack element
        processStackElement(elementIndex);// Process this stack element
    }

    // Return the finalColor value of the last-popped stack element
    return poppedStackElement.finalColor;
}




// Main Method
void main() {
    int width = int(gl_NumWorkGroups.x);
    ivec2 pixel = ivec2(gl_GlobalInvocationID.xy);
    uint pixelIndex = gl_GlobalInvocationID.x + gl_GlobalInvocationID.y * width;


    if (shouldRender(pixelIndex)){
        calcSkyboxCorners();
        Ray worldRay;
        uint rayInfoIndex = pixelIndex * 3;

        if (shouldRender(rayInfoIndex / 3))
        worldRay.start.x = rayStart[rayInfoIndex];
        worldRay.start.y = rayStart[rayInfoIndex + 1];
        worldRay.start.z = rayStart[rayInfoIndex + 2];

        worldRay.dir.x = rayDir[rayInfoIndex];
        worldRay.dir.y = rayDir[rayInfoIndex + 1];
        worldRay.dir.z = rayDir[rayInfoIndex + 2];
        // ---------------------------------------------

        vec3 color = raytrace(worldRay);
        imageStore(outputTexture, pixel, vec4(color, 1.0));
        pixelList[pixelIndex] = STATE_DRAWN;
    }
}
