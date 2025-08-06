#version 150

uniform sampler2D MainSampler;
uniform sampler2D MainDepthSampler;

uniform vec3 FocusPos;
uniform float FocusRange;
uniform float BlurRange;
uniform float Strength;
uniform mat4 InverseViewProjectionMat;
uniform vec4 DebugNearCol;
uniform vec4 DebugFarCol;

in vec2 texCoord;
out vec4 fragColor;

void main() {
	float depth = texture(MainDepthSampler, texCoord).r;
	vec4 clipPos;
	clipPos.xy = texCoord * 2.0 - 1.0;
	clipPos.z = depth * 2.0 - 1.0;
	clipPos.w = 1.0;
	vec4 homogenousPos = InverseViewProjectionMat * clipPos;
	vec3 worldPos = homogenousPos.xyz / homogenousPos.w;
	vec3 diff = worldPos - FocusPos;
	float distSq = dot(diff, diff);

	float blur = 1.0;

	if (distSq < FocusRange * FocusRange) {
		blur = 0.0;
	} else if (distSq < BlurRange * BlurRange) {
		blur = (sqrt(distSq) - FocusRange) / (BlurRange - FocusRange);
	}

	if (Strength <= 0.0) {
		fragColor = mix(DebugNearCol, DebugFarCol, blur);
		return;
	}

	float blurStrength = Strength * blur;
}
