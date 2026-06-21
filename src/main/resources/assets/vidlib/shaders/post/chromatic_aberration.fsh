#version 330

uniform sampler2D InSampler;

layout(std140) uniform Strength {
	float Value;
} StrengthUniform;

layout(std140) uniform Angle {
	float Value;
} AngleUniform;

layout(std140) uniform FocusPos {
	vec2 Value;
} FocusPosUniform;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	vec2 toCenter;
	float strength = StrengthUniform.Value;
	float angle = AngleUniform.Value;
	vec2 focusPos = FocusPosUniform.Value;

	if (angle >= 0.0) {
		toCenter = vec2(cos(angle), sin(angle)) * strength;
	} else {
		toCenter = (focusPos - (texCoord * 2.0 - 1.0)) * abs(strength);
	}

	vec3 c;

	if (strength > 0.0) {
		fragColor = vec4(texture(InSampler, texCoord).r, texture(InSampler, texCoord + toCenter).g, texture(InSampler, texCoord + toCenter * 2.0).b, 1.0);
	} else {
		fragColor = vec4(texture(InSampler, texCoord + toCenter * 2.0).r, texture(InSampler, texCoord + toCenter).g, texture(InSampler, texCoord).b, 1.0);
	}
}
