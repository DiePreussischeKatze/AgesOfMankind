#version 450

uniform vec4 color;

out vec4 FragColor;

void main() {
    FragColor = vec4(color);
}
