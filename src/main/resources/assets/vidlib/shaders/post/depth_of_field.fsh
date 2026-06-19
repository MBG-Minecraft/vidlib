#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(std140) uniform InverseViewProjectionMat {
	mat4 Value;
} InverseViewProjectionMatUniform;

layout(std140) uniform FocusPos {
	vec3 Value;
} FocusPosUniform;

layout(std140) uniform FocusRange {
	float Value;
} FocusRangeUniform;

layout(std140) uniform BlurRange {
	float Value;
} BlurRangeUniform;

layout(std140) uniform Strength {
	float Value;
} StrengthUniform;

layout(std140) uniform Shape {
	int Value;
} ShapeUniform;

layout(std140) uniform DebugNearCol {
	vec4 Value;
} DebugNearColUniform;

layout(std140) uniform DebugFarCol {
	vec4 Value;
} DebugFarColUniform;

layout(std140) uniform BlurMode {
	int Value;
} BlurModeUniform;

in vec2 texCoord;
in vec2 oneTexel;

out vec4 fragColor;

float blurAt(in vec2 coord) {
	float depth = texture(InDepthSampler, coord).r;
	vec4 clipPos;
	clipPos.xy = coord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMatUniform.Value * clipPos;
	vec3 worldPos = homogenousPos.xyz / homogenousPos.w;
	vec3 diff = worldPos - FocusPosUniform.Value;
	float distSq = ShapeUniform.Value == 1 ? dot(diff.xz, diff.xz) : dot(diff, diff);

	if (distSq < FocusRangeUniform.Value * FocusRangeUniform.Value) {
		return 0.0;
	} else if (distSq < BlurRangeUniform.Value * BlurRangeUniform.Value) {
		return (sqrt(distSq) - FocusRangeUniform.Value) / (BlurRangeUniform.Value - FocusRangeUniform.Value);
	} else {
		return 1.0;
	}
}

void main() {
	float blur = blurAt(texCoord);

	if (StrengthUniform.Value <= 0.0) {
		fragColor = mix(DebugNearColUniform.Value, DebugFarColUniform.Value, blur);
		return;
	}

	float size = StrengthUniform.Value * blur;

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

			if (dist <= maxDist && BlurModeUniform.Value == 0 ? (blurAt(texCoord2) >= blur) : BlurModeUniform.Value == 1 ? (blurAt(texCoord2) > 0.0) : BlurModeUniform.Value == 2) {
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
