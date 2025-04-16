#version 150

in vec3 Position;
in vec2 UV0;
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;

out vec4 vertexColor;
out vec2 texCoord0;

vec4 normal_light(vec3 normal) {
	float light0 = max(0.0, dot(normalize(vec3(0.3, 1.8, -1.8)), normal));
	float light1 = max(0.0, dot(normalize(vec3(-0.3, 1.8, 1.8)), normal));
	float diffuse = min(1.0, (light0 + light1) * 0.4 + 0.7);
	return vec4(diffuse, diffuse, diffuse, 1.0);
}

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);
	vec4 normal = ProjMat * ModelViewMat * vec4(Normal, 0.0);
	vertexColor = normal_light(normal.xyz);
	texCoord0 = UV0;
}
