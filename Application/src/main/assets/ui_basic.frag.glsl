precision mediump float;

uniform sampler2D sTexture;

varying vec2 vTexCoord;

void main ()
{
    gl_FragColor = vec4(0,0,1,0.5);//texture2D(sTexture,vTexCoord);
}