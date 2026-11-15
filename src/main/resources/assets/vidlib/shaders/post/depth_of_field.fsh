#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform VidLibDepthOfField {
	mat4 InverseViewProjectionMat;
	vec3 FocusPos;
	float FocusRange;
	float BlurRange;
	float Strength;
	int Shape;
	int BlurMode;
	vec4 DebugNearCol;
	vec4 DebugFarCol;
};

layout(location = 0) out vec4 fragColor;

vec3 clip(vec2 coord) {
	float depth = texture(InDepthSampler, coord).r;
	vec4 clipPos;
	clipPos.xy = coord * 2.0 - 1.0;
	clipPos.z = depth; // * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	return homogenousPos.xyz / homogenousPos.w;
}

float blurAt(vec2 coord) {
	vec3 worldPos = clip(coord);
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

vec4 finalColor() {
	vec2 oneTexel = 1.0 / InSize;

	float blur = blurAt(texCoord);

	if (Strength <= 0.0) {
		vec3 original = texture(InSampler, texCoord).rgb;
		vec3 debugNear = mix(original, DebugNearCol.rgb, DebugNearCol.a);
		vec3 debugFar = mix(original, DebugFarCol.rgb, DebugFarCol.a);
		return vec4(mix(debugNear, debugFar, blur), 1.0);
	}

	float size = Strength * blur;

	if (size <= 0.0) {
		return texture(InSampler, texCoord);
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
		return texture(InSampler, texCoord);
	}

	return vec4(result / count, 1.0);
}

void main() {
	fragColor = finalColor();
}
