#version 150

uniform sampler2D InSampler;
uniform float Size;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	vec3 result = vec3(0.0);
	float count = 0.0;
	float max = 0.0;
	float size = ceil(Size);
	float maxDist = Size * Size;

	for (float x = -size; x <= size; x += 1.0) {
		for (float y = -size; y <= size; y += 1.0) {
			float dist = x * x + y * y;

			if (dist <= maxDist) {
				vec4 c = texture(InSampler, texCoord + oneTexel * vec2(x, y));

				if (c.a >= 0.01) {
					result += c.rgb;
					count += 1.0;
				}

				max += 1.0;
			}
		}
	}

	if (count == 0.0 || count == max) {
		discard;
	}

	fragColor = vec4(result / count, 1.0);
}
