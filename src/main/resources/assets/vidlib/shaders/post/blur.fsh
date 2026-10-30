#version 410 core

uniform sampler2D InSampler;

layout (location = 0) in vec2 texCoord;
layout (location = 1) in vec2 oneTexel;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform BlurConfig {
	vec2 blurDirection;
	float radius;
};

layout (location = 0) out vec4 fragColor;

void main() {
	vec2 sampleStep = oneTexel * blurDirection;
	float actualRadius = radius >= 0.5 ? round(radius) : 1.0;

	vec4 blurred = vec4(0.0);

	for (float a = -actualRadius + 0.5; a <= actualRadius; a += 2.0) {
		blurred += texture(InSampler, texCoord + sampleStep * a);
	}

	blurred += texture(InSampler, texCoord + sampleStep * actualRadius) / 2.0;
	fragColor = vec4((blurred / (actualRadius + 0.5)).rgb, blurred.a);
}
