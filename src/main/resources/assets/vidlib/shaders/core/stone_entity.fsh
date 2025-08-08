#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in float vertexDistance;
in vec4 vertexColor;
in vec4 lightMapColor;
in vec4 overlayColor;
in vec2 texCoord0;

out vec4 fragColor;

float hash(float n) { return fract(sin(n) * 1e4); }
float hash(vec2 p) { return fract(1e4 * sin(17.0 * p.x + p.y * 0.1) * (0.1 + abs(sin(p.y * 13.0 + p.x)))); }

float noise(vec2 x) {
	vec2 i = floor(x);
	vec2 f = fract(x);
	float a = hash(i);
	float b = hash(i + vec2(1.0, 0.0));
	float c = hash(i + vec2(0.0, 1.0));
	float d = hash(i + vec2(1.0, 1.0));
	vec2 u = f * f * (3.0 - 2.0 * f);
	return mix(a, b, u.x) + (c - a) * u.y * (1.0 - u.x) + (d - b) * u.x * u.y;
}

void main() {
	vec4 color = texture(Sampler0, texCoord0);

	if (color.a < 0.1) {
		discard;
	}

	color *= vertexColor * ColorModulator;
	color.rgb = mix(overlayColor.rgb, color.rgb, overlayColor.a);
	color *= lightMapColor;
	float grey = mix(0.4, 1.0, 0.21 * color.r + 0.71 * color.g + 0.07 * color.b);
	vec2 itexCoord0 = floor(texCoord0 * 64.0) / 64.0;
	float n1 = (noise(itexCoord0 * 35.0) + 1.0) / 2.0;
	float n2 = (noise(itexCoord0 * 55.0) + 1.0) / 2.0;
	grey = mix(0.3, 1.0, grey) * mix(0.7, 1.0, n1 * n2);
	fragColor = vec4(grey, grey, grey, color.a);
}
