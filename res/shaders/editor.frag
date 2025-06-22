#version 450 core

out vec4 FragColor;

uniform vec3 color;

void main() {
    //0.8, 0.6, 0.1
    FragColor = vec4(color, 1);
}