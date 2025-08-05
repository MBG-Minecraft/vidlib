#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform float DOFFocus;
uniform float DOFFocusRange;
uniform float DOFBlurRange;
uniform float DOFStrength;
uniform mat4 InverseWorldMat;

in vec2 texCoord;
out vec4 fragColor;

void main() {
	fragColor = vec4(1.0, 0.0, 0.0, clamp(DOFStrength, 0.0, 8.0) / 8.0);
}
