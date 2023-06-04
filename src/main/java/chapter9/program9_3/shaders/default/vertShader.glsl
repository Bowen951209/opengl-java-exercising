#version 430

layout (location=0) in vec3 vertPos;
uniform mat4 proj_matrix;
uniform mat4 mv_matrix;

void main(void){
	gl_Position = proj_matrix * mv_matrix * vec4(vertPos,1.0);
}
