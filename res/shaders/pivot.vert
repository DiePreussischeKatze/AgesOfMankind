// vert
#version 450

layout(location = 0) in vec2 a_Pos;
layout(std430, binding = 1) buffer pos {
    vec2 positions[];
};

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

void main() {
    gl_Position = vec4((a_Pos.x + positions[gl_InstanceID].x + cameraPosition.x) * cameraPosition.z, (a_Pos.y + positions[gl_InstanceID].y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, -0.002, 1.0);
}
