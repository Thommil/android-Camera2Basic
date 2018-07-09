#ifdef GL_ES
    #define LOWP lowp
    precision mediump float;
#else
    #define LOWP
#endif

varying vec2 vTextCoordAttr;
varying LOWP vec4 vColorAttr;

uniform sampler2D texture1i;

void main()
{
    //gl_FragColor = vColorAttr * texture2D(texture1i, vTextCoordAttr);
    gl_FragColor = texture2D(texture1i, vTextCoordAttr);
}