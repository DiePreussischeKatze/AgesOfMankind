#version 450 core

// I added the shader because I'll need more than just positions later on

layout(location = 0) in vec2 a_Pos;

layout(std430, binding = 2) buffer armies {
    float data[];
};

layout(std140, binding = 0) uniform SharedUniforms {
    vec4 cameraPosition;
};

out float selected;

void main() {
    gl_Position = vec4((a_Pos.x + data[gl_InstanceID * 3] + cameraPosition.x) * cameraPosition.z, (a_Pos.y + data[gl_InstanceID * 3 + 1] + cameraPosition.y) * cameraPosition.z * cameraPosition.w, -0.002, 1.0);
    selected = data[gl_InstanceID * 3 + 2];
}
