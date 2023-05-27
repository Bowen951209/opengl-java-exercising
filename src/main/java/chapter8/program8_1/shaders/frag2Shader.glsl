#version 430

in vec3 vNormal, vLightDir, vVertPos, vHalfVec;
in vec4 shadow_coord;
out vec4 fragColor;

struct PositionalLight
{ vec4 ambient, diffuse, specular;
    vec3 position;
};

struct Material
{ vec4 ambient, diffuse, specular;
    float shininess;
};

uniform vec4 globalAmbient;
uniform PositionalLight light;
uniform Material material;
uniform mat4 mv_matrix;
uniform mat4 proj_matrix;
uniform mat4 norm_matrix;
uniform mat4 shadowMVP;
layout (binding=0) uniform sampler2DShadow shadowTex;

float lookUp(float ox, float oy) {
    float t = textureProj(shadowTex, shadow_coord + vec4(ox * 1f/1500f * shadow_coord.w, oy * 1f/1000f * shadow_coord.w, -.01, .0));
    // 第三個參數"-.01"是用於消除陰影痤瘡的偏移量
    return t;
}

void main(void) {
    float shadowFactor = .0f;
    vec3 L = normalize(vLightDir);
    vec3 N = normalize(vNormal);
    vec3 V = normalize(-vVertPos);
    vec3 H = normalize(vHalfVec);

    //-------此部分生成一個4採樣抖動的柔和陰影
    float swidth = 2.5f;// shadow width 可調整的陰影擴散量
    //根據glFragCoord mod 2 生成4種採樣模式中的一種
    vec2 offset = mod(floor(gl_FragCoord.xy), 2f) * swidth;
    shadowFactor += lookUp(-1.5f * swidth + offset.x, 1.5 * swidth - offset.y);
    shadowFactor += lookUp(-1.5f * swidth + offset.x, -.5 * swidth - offset.y);
    shadowFactor += lookUp(.5f * swidth + offset.x, 1.5 * swidth - offset.y);
    shadowFactor += lookUp(-.5f * swidth + offset.x, -.5 * swidth - offset.y);
    // shadowFactor 是4個採樣點的平均值


    float inShadow = textureProj(shadowTex, shadow_coord);

    vec4 shadowColor = globalAmbient * material.ambient
    + light.ambient * material.ambient;
    vec4 lightedColor = light.diffuse * material.diffuse * max(dot(L, N), 0.0)
    + light.specular * material.specular
    * pow(max(dot(H, N), 0.0), material.shininess*3.0);


   	fragColor = vec4(shadowColor.xyz + shadowFactor * (lightedColor.xyz), 1f);
}
