#version 150

uniform sampler2D InSampler;
uniform vec2 InSize;

in vec2 texCoord;

out vec4 fragColor;

const float SCALE = 24.0;

void main() {
	vec2 s = texCoord * InSize / SCALE;
	vec2 m = SCALE / InSize;

	float a
	= texture(InSampler, floor(s + vec2(0.5, 0.5)) * m).a

	+ texture(InSampler, floor(s + vec2(1.5, 0.5)) * m).a
	+ texture(InSampler, floor(s + vec2(0.5, 1.5)) * m).a
	+ texture(InSampler, floor(s + vec2(-0.5, 0.5)) * m).a
	+ texture(InSampler, floor(s + vec2(0.5, -0.5)) * m).a

	+ texture(InSampler, floor(s + vec2(1.5, 1.5)) * m).a
	+ texture(InSampler, floor(s + vec2(1.5, -0.5)) * m).a
	+ texture(InSampler, floor(s + vec2(-0.5, 1.5)) * m).a
	+ texture(InSampler, floor(s + vec2(-0.5, -0.5)) * m).a;

	if (a < 0.01) {
		discard;
	}

	fragColor = vec4(0.0, 0.0, 0.0, 1.0);
}
