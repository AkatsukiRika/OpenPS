precision mediump float;
uniform sampler2D uTexture;
varying vec2 vTexCoord;
uniform lowp float saturation;

const mediump vec3 luminanceWeighting = vec3(0.2125, 0.7154, 0.0721);

void main() {
    lowp vec4 color = texture2D(uTexture, vec2(vTexCoord.x, 1.0 - vTexCoord.y));
    lowp float luminance = dot(color.rgb, luminanceWeighting);
    lowp vec3 greyScaleColor = vec3(luminance);

    gl_FragColor = vec4(mix(greyScaleColor, color.rgb, saturation), color.a);
}