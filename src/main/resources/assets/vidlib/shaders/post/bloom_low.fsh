#version 150

uniform sampler2D InSampler;
uniform sampler2D DepthBeforeParticlesSampler;
uniform sampler2D DepthAfterParticlesSampler;
uniform vec2 InSize;
uniform vec2 OutSize;

in vec2 texCoord;

out vec4 fragColor;

void main() {
	float depthBefore = texture(DepthBeforeParticlesSampler, texCoord).r;
	float depthAfter = texture(DepthAfterParticlesSampler, texCoord).r;

	if (depthAfter < depthBefore) {
		discard;
	}

	int count = 0;
	vec2 scale = vec2(1.0 / (InSize.x * 1.0), 1.0 / (InSize.y * 1.0));
	// vec2 scale = vec2(0.001);

	for (int x = 0; x < 32; x++) {
		for (int y = 0; y < 32; y++) {
			if (texture(InSampler, texCoord + vec2(float(x) - 16.0, float(y) - 16.0) * scale).a > 0.0) {
				count++;
			}
		}
	}

	if (count == 0) {
		discard;
	} else if (count == 1024) {
		fragColor = vec4(1.0, 0.0, 0.0, 1.0);
	} else {
		fragColor = vec4(0.0, 0.0, 1.0, 1.0);
	}
}
