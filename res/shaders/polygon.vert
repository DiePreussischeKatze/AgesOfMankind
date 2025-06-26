// vert
#version 450

layout(location = 0) in vec2 a_Pos;
layout(location = 1) in vec3 a_Color;

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

out vec3 color;

void main() {
    gl_Position = vec4((a_Pos.x + cameraPosition.x) * cameraPosition.z, (a_Pos.y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, 0, 1.0);
    color = a_Color;
}
