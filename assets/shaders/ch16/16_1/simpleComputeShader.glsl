#version 430

layout (local_size_x=1) in;
layout(binding=0) buffer inputBuffer1 { float inVals1[]; };
layout(binding=1) buffer inputBuffer2 { float inVals2[]; };
layout(binding=2) buffer outputBuffer { float outVals[]; };

void main() {
	uint thisRun = gl_GlobalInvocationID.x;
	outVals[thisRun] = inVals1[thisRun] + inVals2[thisRun];
}