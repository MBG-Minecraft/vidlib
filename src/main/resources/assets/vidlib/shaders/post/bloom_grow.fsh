#version 150

uniform sampler2D InSampler;
uniform vec2 InSize;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

bool e(float x, float y) {
	return texture(InSampler, texCoord + oneTexel * vec2(x, y)).a > 0.0;
}

void main() {
	vec4 c = texture(InSampler, texCoord);

	if (c.a > 0.0) {
		if (c.r > 0.0) {
			discard;
		} else {
			fragColor = vec4(0.0, 0.0, 1.0, 1.0);
		}
	} else if (e(1.0, 0.0) || e(0.0, 1.0) || e(-1.0, 0.0) || e(0.0, -1.0) || e(1.0, 1.0) || e(-1.0, 1.0) || e(-1.0, -1.0) || e(1.0, -1.0)) {
		fragColor = vec4(0.0, 1.0, 0.0, 1.0);
	} else {
		discard;
	}
}
