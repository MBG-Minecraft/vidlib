#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform VidLibAngledChromaticAberration {
	float Strength;
	float Angle;
	vec3 Shift;
};

layout(location = 0) out vec4 fragColor;

void main() {
	vec2 toCenter = vec2(cos(Angle), sin(Angle)) * Strength * 0.1;

	fragColor = vec4(
			texture(InSampler, texCoord + toCenter * Shift.x).r,
			texture(InSampler, texCoord + toCenter * Shift.y).g,
			texture(InSampler, texCoord + toCenter * Shift.z).b,
			1.0
	);
}
