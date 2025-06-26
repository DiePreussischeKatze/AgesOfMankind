// frag
#version 450

out vec4 FragColor;

uniform sampler2D tex;

in vec2 uv;

void main() {
    FragColor = texture(tex, uv);
}
