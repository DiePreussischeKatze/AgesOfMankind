#version 450

layout(location = 0) in vec2 a_Pos;
// could someone tell me how to change the binding of a ssbo in the code instead of having to create a new shader
// just to change the binding
layout(std430, binding = 2) buffer pos {
    vec2 positions[];
};

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

void main() {
    gl_Position = vec4((a_Pos.x + positions[gl_InstanceID].x + cameraPosition.x) * cameraPosition.z, (a_Pos.y + positions[gl_InstanceID].y + cameraPosition.y) * cameraPosition.z * cameraPosition.w, -0.001, 1.0);
}
