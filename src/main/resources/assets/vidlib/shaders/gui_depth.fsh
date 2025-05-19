#version 150

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;

in vec2 texCoord0;
in vec4 vertexColor;

out vec4 fragColor;

void main() {
	float d = texture(Sampler0, texCoord0).r;
	float d1 = d * d * d * d * d * d;
    vec4 color = vec4(d1, d1, d1, 1.0) * vertexColor;
    if (color.a == 0.0) {
        discard;
    }
    fragColor = color * ColorModulator;
}
