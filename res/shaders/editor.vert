// vert
#version 450 core

layout (location = 0) in vec2 a_pos;

uniform vec2 offset;

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

void main() {
    gl_Position = vec4((a_pos.x + offset.x + cameraPosition.x) * cameraPosition.z, (a_pos.y + offset.y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, -0.003, 1);
}