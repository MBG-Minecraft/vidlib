#version 330
#extension GL_ARB_separate_shader_objects : require

// @formatter:off
// 1.21.5 imported <minecraft:light.glsl> for minecraft_mix_light()/minecraft_sample_lightmap().
// 26.3's MATRICES_FOG_SNIPPET no longer declares the Lighting UBO, so those two helpers are
// inlined here verbatim instead (identical maths, no extra bind group).
// @formatter:on

#include < minecraft:projection.glsl >
#include < minecraft:dynamictransforms.glsl >

// PhysicsParticlesRenderTypes.FORMAT = Position, Color, UV0, UV2, Normal
layout(location = 0) in vec3 Position;
layout(location = 1) in vec4 Color;
layout(location = 2) in vec2 UV0;
layout(location = 3) in ivec2 UV2;
layout(location = 4) in vec3 Normal;

uniform sampler2D Sampler2;

layout(location = 2) out vec4 vertexColor;
layout(location = 6) out vec2 texCoord0;

vec4 mix_light(vec3 lightDir0, vec3 lightDir1, vec3 normal, vec4 color) {
	vec2 light = max(vec2(0.0), vec2(dot(lightDir0, normal), dot(lightDir1, normal)));
	float lightAccum = min(1.0, (light.x + light.y) * 0.6 + 0.4);
	return vec4(color.rgb * lightAccum, color.a);
}

vec4 sample_lightmap(sampler2D lightMap, ivec2 uv) {
	return texture(lightMap, clamp((uv / 256.0) + 0.5 / 16.0, vec2(0.5 / 16.0), vec2(15.5 / 16.0)));
}

void main() {
	gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

	vertexColor = mix_light(vec3(0.2, 1.0, -0.7), vec3(-0.2, 1.0, 0.7), Normal, Color) * sample_lightmap(Sampler2, UV2);
	texCoord0 = UV0;
}
