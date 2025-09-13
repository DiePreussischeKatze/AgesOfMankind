#version 450

out vec4 FragColor;

in float selected;

void main() {
    if (selected < 0.9) {
        FragColor = vec4(0.8, 0.2, 0, 1);
    } else {
        FragColor = vec4(0.7, 0.7, 0.9, 1);
    }
}
