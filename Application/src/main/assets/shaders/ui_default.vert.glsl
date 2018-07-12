attribute vec2 positionAttr;
attribute vec2 textCoordAttr;
attribute vec4 colorAttr;

uniform vec2 screenRatio2f;

varying vec2 vTextCoordAttr;
varying vec4 vColorAttr;

void main()
{
    vColorAttr = colorAttr;
    vColorAttr.a = vColorAttr.a * (255.0/254.0);
    vTextCoordAttr = textCoordAttr;
    gl_Position =  vec4(positionAttr.x * screenRatio2f.x, positionAttr.y * screenRatio2f.y, 0.0, 1.0);
}

