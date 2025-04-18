#version 150

uniform sampler2D InSampler;
uniform vec2 OutSize;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

void main() {
	vec3 result = vec3(0.0);
	float count = 0.0;
	float size = max(1.0, OutSize.x / 1920.0 * 3.0);
	float csize = ceil(size);
	float maxDist = size * size;
	float intensity = 1.0;

	if(texture(InSampler, texCoord).a < 0.01) {
		for (float x = -csize; x <= csize; x += 1.0) {
			for (float y = -csize; y <= csize; y += 1.0) {
				float dist = x * x + y * y;

				if (dist <= maxDist) {
					vec4 c = texture(InSampler, texCoord + oneTexel * vec2(x, y));

					if (c.a >= 0.01) {
						result += c.rgb;
						count += 1.0;
						intensity = min(intensity, clamp(sqrt(dist - 1.0) / size, 0.0, 1.0));
					}
				}
			}
		}
	}

	if (count == 0.0) {
		discard;
	}

	fragColor = vec4(result / count, 1.0 - intensity);
}
