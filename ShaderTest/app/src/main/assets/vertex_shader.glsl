attribute vec4 vPosition;
attribute vec2 aTexCoord;
varying vec2 vTexCoord;

void main() {
    gl_Position = vPosition;
    vTexCoord = aTexCoord;
}