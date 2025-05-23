#version 150

uniform sampler2D InSampler;
in vec2 texCoord;
out vec4 fragColor;

void main() {
	vec4 c = texture(InSampler, texCoord);

	if (c.a == 0.0) {
		discard;
	}

	fragColor = c;
}
