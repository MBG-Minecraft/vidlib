#version 150

uniform sampler2D InSampler;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	vec3 result = vec3(0.0);
	float count = 0.0;
	float maxCount = 0.0;
	float size = max(1.0, OutSize.x / 1920.0 * 2.0);
	float csize = ceil(size);
	float maxDist = size * size;

	for (float x = -csize; x <= csize; x += 1.0) {
		for (float y = -csize; y <= csize; y += 1.0) {
			float dist = x * x + y * y;

			if (dist <= maxDist) {
				vec4 c = texture(InSampler, texCoord + oneTexel * vec2(x, y));

				if (c.a >= 0.01) {
					result += c.rgb;
					count += 1.0;
				}

				maxCount += 1.0;
			}
		}
	}

	if (count == 0.0 || maxCount == 0.0 || count == maxCount) {
		discard;
	}

	fragColor = vec4(result / count, min(1.0, (count + 1.0) / maxCount));
}
