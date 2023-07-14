#version 430

layout (vertices = 4) out;

in vec2 tc[];
out vec2 tcs_out[];

uniform mat4 mv_matrix;
uniform mat4 p_matrix;
layout (binding = 0) uniform sampler2D tex_color;
layout (binding = 1) uniform sampler2D tex_height;

void main(void) {
    float subdivision = 16.0;

    if (gl_InvocationID == 0) {
        mat4 mvp_matrix = p_matrix * mv_matrix;
        // Control points in screen sace:
        vec4 p0 = mvp_matrix * gl_in[0].gl_Position;
        vec4 p1 = mvp_matrix * gl_in[2].gl_Position;
        vec4 p2 = mvp_matrix * gl_in[1].gl_Position;
        p0 /= p0.w;
        p1 /= p1.w;
        p2 /= p2.w;
        float widthLevel = length(p2.xy - p0.xy) * subdivision + 1.0; // 1.0 is because length could be 0, add 1 can avoid that.
        float heightLevel = length(p1.xy - p0.xy) * subdivision + 1.0;


        gl_TessLevelOuter[0] = heightLevel;
        gl_TessLevelOuter[1] = widthLevel;
        gl_TessLevelOuter[2] = heightLevel;
        gl_TessLevelOuter[3] = widthLevel;
        gl_TessLevelInner[0] = widthLevel;
        gl_TessLevelInner[1] = heightLevel;
    }

    tcs_out[gl_InvocationID] = tc[gl_InvocationID];
    gl_out[gl_InvocationID].gl_Position = gl_in[gl_InvocationID].gl_Position;
}