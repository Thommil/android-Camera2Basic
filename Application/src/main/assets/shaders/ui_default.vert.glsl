attribute vec2 positionAttr;
attribute vec2 textCoordAttr;
attribute float colorAttr;

varying vec2 vTextCoordAttr;
varying float vColorAttr;

void main()
{
    vTextCoordAttr = textCoordAttr;
    vColorAttr = colorAttr;
    gl_Position =  vec4(positionAttr.x, positionAttr.y, 0.0, 1.0);
}

