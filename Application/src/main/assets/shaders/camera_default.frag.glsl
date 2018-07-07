#extension GL_OES_EGL_image_external : require

precision mediump float;

uniform samplerExternalOES texture1i;

varying vec2 vTextCoordAttr;

void main ()
{
    vec4 cameraColor = texture2D(texture1i, vTextCoordAttr);
    gl_FragColor = cameraColor;
}