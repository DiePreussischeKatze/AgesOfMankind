#version 450

layout(location = 0) in vec2 a_Pos;

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

void main() {
    gl_Position = vec4((a_Pos.x + cameraPosition.x) * cameraPosition.z, (a_Pos.y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, -0.005, 1.0);
}
