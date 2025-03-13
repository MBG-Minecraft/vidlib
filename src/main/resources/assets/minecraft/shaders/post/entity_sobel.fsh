#version 150

uniform sampler2D InSampler;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	vec3 result = vec3(0.0);
	float count = 0.0;

	for (float x = -1.0; x <= 1.0; x += 1.0) {
		for (float y = -1.0; y <= 1.0; y += 1.0) {
			vec4 c = texture(InSampler, texCoord + oneTexel * vec2(x, y));

			if (c.a >= 0.01) {
				result += c.rgb;
				count += 1.0;
			}
		}
	}

	if (count == 0.0 || count == 9.0) {
		discard;
	}

	fragColor = vec4(result / count, 1.0);
}
