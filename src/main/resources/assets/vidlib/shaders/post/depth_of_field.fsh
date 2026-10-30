#version 410 core

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform InverseViewProjectionMat {
	mat4 inverseViewProjectionMat;
};

layout(std140) uniform FocusPos {
	vec3 focusPos;
};

layout(std140) uniform FocusRange {
	float focusRange;
};

layout(std140) uniform BlurRange {
	float blurRange;
};

layout(std140) uniform Strength {
	float strength;
};

layout(std140) uniform Shape {
	int shape;
};

layout(std140) uniform DebugNearCol {
	vec4 debugNearCol;
};

layout(std140) uniform DebugFarCol {
	vec4 debugFarCol;
};

layout(std140) uniform BlurMode {
	int blurMode;
};

layout (location = 0) in vec2 texCoord;
layout (location = 1) in vec2 oneTexel;

layout (location = 0) out vec4 fragColor;

float blurAt(in vec2 coord) {
	float depth = texture(InDepthSampler, coord).r;
	vec4 clipPos;
	clipPos.xy = coord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = inverseViewProjectionMat * clipPos;
	vec3 worldPos = homogenousPos.xyz / homogenousPos.w;
	vec3 diff = worldPos - focusPos;
	float distSq = shape == 1 ? dot(diff.xz, diff.xz) : dot(diff, diff);

	if (distSq < focusRange * focusRange) {
		return 0.0;
	} else if (distSq < blurRange * blurRange) {
		return (sqrt(distSq) - focusRange) / (blurRange - focusRange);
	} else {
		return 1.0;
	}
}

void main() {
	float blur = blurAt(texCoord);

	if (strength <= 0.0) {
		fragColor = mix(debugNearCol, debugFarCol, blur);
		return;
	}

	float size = strength * blur;

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

			if (dist <= maxDist && (blurMode == 0 ? (blurAt(texCoord2) >= blur) : blurMode == 1 ? (blurAt(texCoord2) > 0.0) : blurMode == 2)) {
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
