//position
attribute vec2 positionAttr;

//camera transform and texture
uniform mat4 mvpMatrix4fv;
attribute vec4 textCoordAttr;

//tex coords
varying vec2 vTextCoordAttr;

void main()
{
    //camera texcoord needs to be manipulated by the transform given back from the system
    vTextCoordAttr = (mvpMatrix4fv * textCoordAttr).xy;
    gl_Position = vec4(positionAttr.x, positionAttr.y, 0.0, 1.0);
}