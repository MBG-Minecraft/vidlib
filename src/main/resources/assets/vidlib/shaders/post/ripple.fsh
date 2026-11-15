#version 330
#extension GL_ARB_separate_shader_objects : require

uniform sampler2D InSampler;

layout(location = 0) in vec2 texCoord;

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

layout(std140) uniform VidLibRipple {
	float GameTimeSecond;
	vec2 FocusPos;
	float Intensity;
	float Detail;
	float Speed;
};

layout(location = 0) out vec4 fragColor;

void main() {
	// Source: https://www.shadertoy.com/view/ldBXDD
	vec2 cPos = -1.0 + 2.0 * texCoord + FocusPos;
	float cLength = length(cPos);
	vec2 uv = texCoord + (cPos / cLength) * cos(cLength * Detail - GameTimeSecond * Speed) * Intensity * 0.01;
	fragColor = vec4(texture(InSampler, uv).rgb, 1.0);
}
