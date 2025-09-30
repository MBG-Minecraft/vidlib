#version 410 core

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

uniform mat4 InverseViewProjectionMat;
uniform vec3 FocusPos;
uniform float FocusRange;
uniform float BlurRange;
uniform float Strength;
uniform int Shape;
uniform vec4 DebugNearCol;
uniform vec4 DebugFarCol;
uniform int BlurMode;

in vec2 texCoord;
in vec2 oneTexel;

layout (location = 0) out vec4 fragColor;

float blurAt(in vec2 coord) {
	float depth = texture(InDepthSampler, coord).r;
	vec4 clipPos;
	clipPos.xy = coord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	vec3 worldPos = homogenousPos.xyz / homogenousPos.w;
	vec3 diff = worldPos - FocusPos;
	float distSq = Shape == 1 ? dot(diff.xz, diff.xz) : dot(diff, diff);

	if (distSq < FocusRange * FocusRange) {
		return 0.0;
	} else if (distSq < BlurRange * BlurRange) {
		return (sqrt(distSq) - FocusRange) / (BlurRange - FocusRange);
	} else {
		return 1.0;
	}
}

void main() {
	float blur = blurAt(texCoord);

	if (Strength <= 0.0) {
		fragColor = mix(DebugNearCol, DebugFarCol, blur);
		return;
	}

	float size = Strength * blur;

	if (size <= 0.0) {
		discard;
	}

	float count = 0.0;
	int csize = int(ceil(size));
	float maxDist = size * size;

	vec3 result = vec3(0.0);

	for (int x = -csize; x <= csize; x += 1) {
		for (int y = -csize; y <= csize; y += 1) {
			if (x == 0 && y == 0) {
				continue;
			}

			float dist = x * x + y * y;
			vec2 texCoord2 = texCoord + oneTexel * vec2(x, y);

			if (dist <= maxDist && BlurMode == 0 ? (blurAt(texCoord2) >= blur) : BlurMode == 1 ? (blurAt(texCoord2) > 0.0) : BlurMode == 2) {
				vec4 c = texture(InSampler, texCoord2);

				if (c.a > 0.0) {
					count += 1.0;
					result += c.rgb;
				}
			}
		}
	}

	if (count <= 0.0) {
		discard;
	}

	fragColor = vec4(result / count, 1.0);
}
