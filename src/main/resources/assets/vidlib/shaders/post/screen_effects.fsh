#version 330

uniform sampler2D InSampler;
uniform sampler2D InDepthSampler;
uniform sampler2D DataSampler;

layout(std140) uniform Count {
	int Value;
} CountUniform;

layout(std140) uniform InverseViewProjectionMat {
	mat4 Value;
} InverseViewProjectionMatUniform;

in vec2 texCoord;

out vec4 fragColor;

uvec4 fetchRGBA8u(ivec2 tc) {
	vec4 s = texelFetch(DataSampler, tc, 0) * 255.0;
	return uvec4(round(s));
}

uint decodeUInt(int x, int y) {
	uvec4 v = fetchRGBA8u(ivec2(x, y));
	return (v.w << 24) | (v.x << 16) | (v.y << 8) | v.z;
}

float decodeFloat(int x, int y) {
	return uintBitsToFloat(decodeUInt(x, y));
}

vec4 decodeColor(int x, int y) {
	return texelFetch(DataSampler, ivec2(x, y), 0);
}

vec3 blend(vec3 src, vec4 dst) {
	return src * (1.0 - dst.a) + dst.rgb * dst.a;
}

vec3 clip(float depth) {
	vec4 clipPos;
	clipPos.xy = texCoord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMatUniform.Value * clipPos;
	return homogenousPos.xyz / homogenousPos.w;
}

void main() {
	if (CountUniform.Value == 0) {
		discard;
	}

	float depth = texture(InDepthSampler, texCoord).r;
	vec3 worldPos = clip(depth);

	if (CountUniform.Value == 999) {
		fragColor = vec4(mod(length(worldPos), 1.0), 0.0, 0.0, 1.0);
		return;
	} else if (CountUniform.Value == 998) {
		fragColor = vec4(1.0, 0.0, 0.0, 1.0);
		return;
	}

	vec3 color = texture(InSampler, texCoord).rgb;

	for (int y = 0; y < 8; y++) {
		if (y >= CountUniform.Value) {
			break;
		}

		int head = int(decodeUInt(0, y));
		int type = head & 15;

		if (type == 0) {
			continue;
		}

		if (type == 1) {
			vec4 col = decodeColor(1, y);
			color = blend(color, col);
		}

		if (type == 998) {
			color = blend(color, vec4(mod(length(worldPos), 1.0), 0.0, 0.0, 1.0));
		}
	}

	fragColor = vec4(color, 1.0);
}
