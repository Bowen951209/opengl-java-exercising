#version 430

in vec3 varyingNormal;
in vec3 varyingLightDir;
in vec3 varyingVertPos;
in vec3 varyingHalfVector;
in vec4 glp;
in vec2 tc;

out vec4 fragColor;

struct PositionalLight {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    vec3 position;
};

struct Material {
    vec4 ambient;
    vec4 diffuse;
    vec4 specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform int isAbove;
uniform float noiseTexSampleY;
uniform float textureScale;

layout(binding = 0) uniform sampler2D reflectionTexture;
layout(binding = 1) uniform sampler2D refractionTexture;
layout(binding = 4) uniform sampler3D noiseTex;

const vec4 blueColor = vec4(0.0, 0.25, 1.0, 1.0);
const float waveStrength = 0.02;
const vec4 fogColor = vec4(0.0, 0.0, 0.2, 1.0);
const float fogStart = 10.0;
const float fogEnd = 300.0;

vec3 estimateWaveNormal(float offset, float mapScale, float hScale) {
    // estimate the normal using the noise texture
    // by looking up three height values around this vertex
    float h1 = (texture(noiseTex, vec3(((tc.s)    )*mapScale, noiseTexSampleY, ((tc.t)+offset)*mapScale))).r * hScale;
    float h2 = (texture(noiseTex, vec3(((tc.s)-offset)*mapScale, noiseTexSampleY, ((tc.t)-offset)*mapScale))).r * hScale;
    float h3 = (texture(noiseTex, vec3(((tc.s)+offset)*mapScale, noiseTexSampleY, ((tc.t)-offset)*mapScale))).r * hScale;
    vec3 v1 = vec3(0, h1, -1);
    vec3 v2 = vec3(-1, h2, 1);
    vec3 v3 = vec3(1, h3, 1);
    vec3 v4 = v2-v1;
    vec3 v5 = v3-v1;
    vec3 normEst = normalize(cross(v4,v5));
    return normEst;
}

void main(void) {
    // -------------------lighting----------------------
    vec3 L = normalize(varyingLightDir);
    vec3 N = estimateWaveNormal(0.0002, textureScale * 0.4, 30.0);

    vec3 V = normalize(-varyingVertPos);
    float cosTheta = dot(L, N);
    vec3 H = normalize(varyingHalfVector);
    float cosPhi = dot(H, N);

    vec3 ambient = ((globalAmbient) + (light.ambient)).xyz;
    vec3 diffuse = light.diffuse.xyz * max(cosTheta, 0.0);
    vec3 specular = light.specular.xyz * pow(max(cosPhi, 0.0), material.shininess);

    //--------------------water reflection & refraction--
    vec2 tcForReflection, tcForRefraction;
    vec4 reflectColor, refractColor, mixColor;


    // desortion
    vec3 estNcb = estimateWaveNormal(.05, textureScale, 0.05);

    float distortStrength = 0.5;
    if (isAbove != 1) distortStrength = 0.0;

    vec2 distorted = tc + estNcb.xz * distortStrength;

    vec2 distortion = texture(noiseTex, vec3((distorted * textureScale).x, noiseTexSampleY, (distorted * textureScale).y)).rg * 2.0 - 1.0;
    distortion *= waveStrength;

    if (isAbove == 1) { // above water
        tcForReflection = vec2(glp.x, -glp.y) / glp.w / 2.0 + 0.5;
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;

        tcForReflection += distortion;
        tcForReflection = clamp(tcForReflection, 0.001, 0.999);// avoid sampling from out of [0, 1]

        tcForRefraction += distortion;
        tcForRefraction = clamp(tcForRefraction, 0.001, 0.999);


        reflectColor = texture(reflectionTexture, tcForReflection);
        reflectColor = vec4(reflectColor.xyz * (ambient + diffuse) + 0.75 * specular, 1.0);
        refractColor = texture(refractionTexture, tcForRefraction);

        vec3 Nfres = normalize(varyingNormal);
        float cosFres = dot(V, Nfres);
        float fresnel = acos(cosFres);
        fresnel = pow(clamp(fresnel - 0.1, 0.0, 1.0), 3.0);

        fragColor = mix(refractColor, reflectColor, fresnel);
    } else { // below water
        tcForRefraction = vec2(glp.x, glp.y) / glp.w / 2.0 + 0.5;
        tcForRefraction += distortion;
        tcForRefraction = clamp(tcForRefraction, 0.001, 0.999);

        refractColor = texture(refractionTexture, tcForRefraction);
        mixColor = 1.8 * blueColor + 1.2 * refractColor;

        float dist = length(varyingVertPos);
        float fogFactor = clamp((fogEnd - dist) / (fogEnd-fogStart), 0.0, 1.0);

        fragColor = vec4(mixColor.xyz * (ambient + diffuse) + 0.75 * specular, 1.0);
        fragColor = mix(fogColor, fragColor, pow(fogFactor, 5));
    }
}
