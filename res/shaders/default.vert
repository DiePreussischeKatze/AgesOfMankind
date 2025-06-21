#version 450 core

layout(location = 0) in vec2 a_Coord;
layout(location = 1) in vec2 a_UV;

out vec2 uv;

// OGL is stupid and cameraPosition.w has to be the aspect ratio, it won't accept a vec3
// for the position and a float for the aspect ratio
layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};


void main() {
    gl_Position = vec4((a_Coord.x + cameraPosition.x) * cameraPosition.z, (a_Coord.y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, 0.001, 1.0);
    uv = a_UV;
}
