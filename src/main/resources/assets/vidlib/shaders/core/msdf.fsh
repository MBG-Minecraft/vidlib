#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

float median(float red, float green, float blue) {
	return max(min(red, green), min(max(red, green), blue));
}

void main() {
	vec4 texel = texture(Sampler0, texCoord0);
	ivec2 sz = textureSize(Sampler0, 0);
	float dx = dFdx(texCoord0.x) * sz.x;
	float dy = dFdy(texCoord0.y) * sz.y;
	float toPixels = 8.0 * inversesqrt(dx * dx + dy * dy);
	float sigDist = median(texel.r, texel.g, texel.b) - 0.5;
	float alpha0 = sigDist * toPixels + 0.5;
	float alpha = clamp(alpha0, 0.0, 1.0);
	fragColor = vec4(vertexColor.rgb, alpha);
}
