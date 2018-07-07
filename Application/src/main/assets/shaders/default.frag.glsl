precision mediump float;

uniform sampler2D texture1i;

varying vec2 vTextCoordAttr;

void main ()
{
    gl_FragColor = texture2D(texture1i,vTextCoordAttr);
}