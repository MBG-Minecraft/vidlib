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
		toCenter = (FocusPos - (texCoord * 2.0 - 1.0)) * Strength;
	}

	float cr = texture(InSampler, texCoord).r;
	float cg = texture(InSampler, texCoord + toCenter).g;
	float cb = texture(InSampler, texCoord + toCenter * 2.0).b;
	fragColor = vec4(cr, cg, cb, 1.0);
}
