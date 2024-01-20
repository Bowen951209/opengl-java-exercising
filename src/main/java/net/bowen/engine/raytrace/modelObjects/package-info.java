
/**
 * <h3>Model objects in this package is for this GLSL specific structure:</h3>
 * <p></p>
 * <pre>
 *     struct Object {
 *          int type;// what type is the object? skybox/sphere/box/plane
 *          float radius;// for OBJ_TYPE_SPHERE
 *          vec3 mins;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE(x and z value are for width and height)
 *          vec3 maxs;// for OBJ_TYPE_BOX
 *          vec3 rotation;// for OBJ_TYPE_BOX / OBJ_TYPE_PLANE
 *          vec3 position;
 *          bool hasColor;
 *          bool hasTexture;
 *          bool isReflective;// whether the object is reflective. For type roombox, this is for enable/disable lighting.
 *          bool isTransparent;
 *          vec3 color;// for hasColor
 *          float reflectivity;// for isReflective
 *          float refractivity;// for isTransparent
 *          float IOR;// for isTransparent
 *          float shininess;
 *          vec4 ambient;
 *          vec4 diffuse;
 *          vec4 specular;
 *     };
 * <pre/>
 *
 */
package net.bowen.engine.raytrace.modelObjects;
