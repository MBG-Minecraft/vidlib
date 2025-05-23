#version 150

uniform sampler2D InSampler;
uniform vec2 InSize;
uniform sampler2D LowSampler;
uniform vec2 LowSize;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	vec4 lowColor = texture(LowSampler, texCoord);

	// if (1.0 == 1.0) { if (lowColor.a == 0) discard; fragColor = vec4(lowColor.rgb, 0.8); return; }

	if (lowColor.a == 0.0) {
		discard;
	}

	vec4 centerColor = texture(InSampler, texCoord);

	if (centerColor.a > 0.0) {
		return;
	}

	float count = 0.0;
	float size = max(1.0, OutSize.x / 1920.0 * 24.0);
	float csize = ceil(size);
	float maxDist = size * size;

	vec3 result = vec3(0.0);
	float intensity = 1.0;
	float maxAlpha = 0.0;

	for (float x = -csize; x <= csize; x += 1.0) {
		for (float y = -csize; y <= csize; y += 1.0) {
			float dist = x * x + y * y;

			if (dist <= maxDist) {
				vec4 c = texture(InSampler, texCoord + oneTexel * vec2(x, y));

				if (c.a > 0.0) {
					count += 1.0;
					result += c.rgb;
					intensity = min(intensity, clamp(sqrt(dist - 1.0) / size, 0.0, 1.0));
					maxAlpha = max(maxAlpha, c.a);
				}
			}
		}
	}

	if (count == 0.0) {
		discard;
	}

	float ra = (1.0 - intensity) * maxAlpha * min(count * 2.0 / (size * size * 3.141592), 1.0);

	if (ra <= 0.0) {
		discard;
	}

	fragColor = vec4(result / count, ra);
}
