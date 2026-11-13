#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform FocusedChromaticAberration {
	float Strength;
	vec2 Focus;
};

layout(location = 0) out vec4 fragColor;

void main() {
	vec2 toCenter = (Focus - (texCoord * 2.0 - 1.0)) * abs(Strength);

	if (Strength > 0.0) {
		fragColor = vec4(
				texture(InSampler, texCoord).r,
				texture(InSampler, texCoord + toCenter).g,
				texture(InSampler, texCoord + toCenter * 2.0).b,
				1.0);
	} else {
		fragColor = vec4(
				texture(InSampler, texCoord + toCenter * 2.0).r,
				texture(InSampler, texCoord + toCenter).g,
				texture(InSampler, texCoord).b,
				1.0);
	}
}