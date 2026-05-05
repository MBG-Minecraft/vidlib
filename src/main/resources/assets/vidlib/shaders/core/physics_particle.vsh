#version 150

in vec3 Position;
in vec2 UV0;
in vec2 UV2;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

	vec3 n = normalize(Normal);

	float brightness = 1.0;
	if (n.y < 0.0) {
		brightness = 0.7 + n.y * 0.4;
	}

	float worldLight = (UV2.x + UV2.y) * 0.5;
	float finalLight = brightness * (0.7 + worldLight * 0.3);
	vertexColor = vec4(finalLight, finalLight, finalLight, 1.0);
	texCoord0 = UV0;
}
