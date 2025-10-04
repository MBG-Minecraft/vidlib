#version 410 core

uniform sampler2D InSampler;

uniform float Strength;
uniform float Angle;
uniform vec2 FocusPos;

in vec2 texCoord;
in vec2 oneTexel;

layout (location = 0) out vec4 fragColor;

void main() {
	vec2 toCenter;

	if (Angle >= 0.0) {
		toCenter = vec2(cos(Angle), sin(Angle)) * Strength;
	} else {
		toCenter = (FocusPos - (texCoord * 2.0 - 1.0)) * abs(Strength);
	}

	vec3 c;

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
