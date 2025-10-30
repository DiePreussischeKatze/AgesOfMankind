#version 450 
layout (location = 0) in vec4 vertex; // <vec2 pos, vec2 tex>

out vec2 TexCoords;

void main() {
    gl_Position = vec4(vertex.xy, -0.0009, 1.00);
    TexCoords = vertex.zw;
}
