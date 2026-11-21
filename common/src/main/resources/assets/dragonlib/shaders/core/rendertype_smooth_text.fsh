#version 150

#moj_import <fog.glsl>

uniform sampler2D Sampler0;

uniform vec4 ColorModulator;
uniform float FogStart;
uniform float FogEnd;
uniform vec4 FogColor;

in float vertexDistance;
in vec4 vertexColor;
in vec2 texCoord0;

out vec4 fragColor;

const int MAX_FOOTPRINT_TAPS = 12;
const float COVERAGE_BOOST = 0.65;

vec4 fetchPremultiplied(ivec2 p, ivec2 size) {
    vec4 c = texelFetch(Sampler0, clamp(p, ivec2(0), size - 1), 0);
    return vec4(c.rgb * c.a, c.a);
}

vec4 sampleSharp(vec2 uvTex, vec2 texelsPerPixel, ivec2 size) {
    vec2 p = uvTex - 0.5;
    vec2 base = floor(p);
    vec2 w = clamp((p - base - 0.5) / max(texelsPerPixel, vec2(0.0001)) + 0.5, 0.0, 1.0);
    ivec2 b = ivec2(base);
    vec4 c00 = fetchPremultiplied(b, size);
    vec4 c10 = fetchPremultiplied(b + ivec2(1, 0), size);
    vec4 c01 = fetchPremultiplied(b + ivec2(0, 1), size);
    vec4 c11 = fetchPremultiplied(b + ivec2(1, 1), size);
    return mix(mix(c00, c10, w.x), mix(c01, c11, w.x), w.y);
}

vec4 sampleFootprint(vec2 uvTex, vec2 dx, vec2 dy, float footprint, ivec2 size) {
    int taps = clamp(int(ceil(footprint)) + 1, 2, MAX_FOOTPRINT_TAPS);
    vec4 sum = vec4(0.0);
    for (int i = 0; i < taps; i++) {
        for (int j = 0; j < taps; j++) {
            vec2 o = (vec2(i, j) + 0.5) / float(taps) - 0.5;
            sum += fetchPremultiplied(ivec2(floor(uvTex + dx * o.x + dy * o.y)), size);
        }
    }
    return sum / float(taps * taps);
}

void main() {
    ivec2 size = textureSize(Sampler0, 0);
    vec2 uvTex = texCoord0 * vec2(size);

    vec2 dx = dFdx(uvTex);
    vec2 dy = dFdy(uvTex);
    vec2 texelsPerPixel = abs(dx) + abs(dy);
    float footprint = max(texelsPerPixel.x, texelsPerPixel.y);

    vec4 tex = footprint <= 1.0 ? sampleSharp(uvTex, texelsPerPixel, size) : sampleFootprint(uvTex, dx, dy, footprint, size);

    vec3 rgb = tex.a > 0.0 ? tex.rgb / tex.a : vec3(0.0);
    float boost = mix(1.0, COVERAGE_BOOST, clamp(footprint - 1.0, 0.0, 1.0));
    float alpha = pow(tex.a, boost);

    vec4 color = vec4(rgb, alpha) * vertexColor * ColorModulator;
    if (color.a < 0.01) {
        discard;
    }
    fragColor = linear_fog(color, vertexDistance, FogStart, FogEnd, FogColor);
}
