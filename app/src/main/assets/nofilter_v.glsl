#version 300 es
attribute vec4 position;
attribute vec4 inputTextureCoordinate;
varing vec2 textureCoordinate;

void main() {
    gl_Position = position;
    textureCoordinate = inputTextureCoordinate.xy;
}