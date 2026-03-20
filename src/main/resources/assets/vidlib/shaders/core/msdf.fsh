#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float median(float red, float green, float blue) {
	return max(min(red, green), min(max(red, green), blue));
}

float screenPxRange() {
	vec2 unitRange = vec2(6.0) / vec2(textureSize(Sampler0, 0));
	vec2 screenTexSize = vec2(1.0) / fwidth(texCoord0);
	return max(0.5 * dot(unitRange, screenTexSize), 1.0);
}

void main() {
	vec4 texel = texture(Sampler0, texCoord0);
	float distance = median(texel.r, texel.g, texel.b);
	float pxRange = screenPxRange();
	float pxDist = pxRange * (distance - 0.5);

	vec3 innerColor = vertexColor.rgb;

	float opacity = clamp(pxDist + 0.5, 0.0, 1.0);

	fragColor = vec4(innerColor * 1.05, opacity);
}
