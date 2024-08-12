precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCoord;

void main() {
    gl_FragColor = texture2D(uTexture, vec2(vTexCoord.x, 1.0 - vTexCoord.y));
}