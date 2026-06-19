#version 330

layout(std140) uniform SamplerInfo {
	vec2 OutSize;
	vec2 InSize;
};

out vec2 texCoord;
out vec2 oneTexel;

void main() {
	vec2 uv = vec2((gl_VertexID << 1) & 2, gl_VertexID & 2);
	gl_Position = vec4(uv * 2.0 - 1.0, 0.0, 1.0);
	oneTexel = 1.0 / InSize;
	texCoord = uv;
}
