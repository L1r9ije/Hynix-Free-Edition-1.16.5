#version 120

uniform vec2 size;
uniform vec3 color0, color1, color2, color3;
uniform vec4 radius;
uniform float alpha;

float signedDistanceField(vec2 p, vec2 b, vec4 r) {
    r.xy = (p.x > 0.0) ? r.xy : r.zw;
    r.x = (p.y > 0.0) ? r.x : r.y;

    vec2 q = abs(p) - b + r.x;

    return min(max(q.x, q.y), 0.0) + length(max(q, 0.0)) - r.x;
}

vec3 createGradient(vec2 pos) {
    return mix(mix(color0.rgb, color1.rgb, pos.y), mix(color2.rgb, color3.rgb, pos.y), pos.x);
}

void main() {
    vec2 halfSize = size * 0.5;
    float sdf = signedDistanceField(halfSize - gl_TexCoord[0].st * size, halfSize - 1.0, radius);
    gl_FragColor = vec4(createGradient(gl_TexCoord[0].st), (1.0 - smoothstep(0.0, 1.0, sdf)) * alpha);
}