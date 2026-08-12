#!/usr/bin/env python3
"""
Generates the launcher icon and Android TV banner as VectorDrawable XML.

Vectors rather than PNGs on purpose: the app already scales its whole layout from the
framebuffer rather than trusting the density a television reports, and a vector icon is the same
idea — one asset that is crisp on a 1080p set and on a 2160p one, with no density buckets to get
wrong.

The wordmark is real type. Glyph outlines are lifted out of DejaVu Serif Bold with fontTools and
written straight into the path data, so nothing depends on a font being present on the device.
DejaVu's licence (Bitstream Vera derived) permits this.
"""

from fontTools.ttLib import TTFont
from fontTools.pens.svgPathPen import SVGPathPen
from fontTools.pens.transformPen import TransformPen
from fontTools.misc.transform import Transform

FONT = "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf"

INK = "#F4EFE9"       # the page
RULE = "#C9BDB2"      # printed terms on the page
COPPER = "#C8825A"    # the signature, and the app's accent everywhere else
DARK = "#14110F"
PANEL = "#2B2421"   # the tile field, a shade above the launcher background so it has an edge

# ---------------------------------------------------------------- type


class Type:
    def __init__(self, path):
        self.font = TTFont(path)
        self.upem = self.font["head"].unitsPerEm
        self.glyphs = self.font.getGlyphSet()
        self.cmap = self.font.getBestCmap()
        self.hmtx = self.font["hmtx"]

    def _names(self, text):
        return [self.cmap[ord(c)] for c in text]

    def advance(self, text, size, tracking):
        """Width of the set line in output units."""
        total = sum(self.hmtx[n][0] for n in self._names(text)) * size / self.upem
        return total + tracking * max(len(text) - 1, 0)

    def size_to_fit(self, text, width, tracking_ratio):
        """The point size at which this string sets to exactly `width`."""
        units = sum(self.hmtx[n][0] for n in self._names(text)) / self.upem
        # tracking is expressed as a fraction of the size, so it scales with it
        return width / (units + tracking_ratio * max(len(text) - 1, 0))

    def path(self, text, size, x, baseline, tracking=0.0):
        """SVG path data for the string, already positioned in output units."""
        scale = size / self.upem
        out = []
        pen_x = x
        for name in self._names(text):
            svg = SVGPathPen(self.glyphs)
            # y is flipped: font space grows upwards, drawable space downwards.
            transform = Transform(scale, 0, 0, -scale, pen_x, baseline)
            self.glyphs[name].draw(TransformPen(svg, transform))
            data = svg.getCommands()
            if data:
                out.append(data)
            pen_x += self.hmtx[name][0] * scale + tracking
        return " ".join(out)


# ---------------------------------------------------------------- the mark

def rounded_rect(x0, y0, x1, y1, r, s, ox, oy):
    """A rounded rectangle in mark space (0..100), emitted in output units."""
    def px(v):
        return round(ox + v * s, 2)

    def py(v):
        return round(oy + v * s, 2)

    rr = round(r * s, 2)
    return (
        f"M{px(x0 + r)},{py(y0)}"
        f"H{px(x1 - r)}"
        f"A{rr},{rr} 0 0 1 {px(x1)},{py(y0 + r)}"
        f"V{py(y1 - r)}"
        f"A{rr},{rr} 0 0 1 {px(x1 - r)},{py(y1)}"
        f"H{px(x0 + r)}"
        f"A{rr},{rr} 0 0 1 {px(x0)},{py(y1 - r)}"
        f"V{py(y0 + r)}"
        f"A{rr},{rr} 0 0 1 {px(x0 + r)},{py(y0)}"
        "Z"
    )


def signature(s, ox, oy):
    """The copper stroke across the page, in output units."""
    # A hand, not a zigzag: a tall opening stroke, undulations that decay as they go, then a
    # long flourish lifting away to the right.
    pts = [
        (32, 76),
        (33.5, 66), (35, 58), (38, 60.5),
        (41, 63), (39, 74), (43, 72),
        (46, 70), (46.5, 62.5), (50, 64.5),
        (53, 66.5), (52, 73), (56, 71.5),
        (60, 70), (63, 67.5), (70, 59.5),
    ]
    p = [(round(ox + x * s, 2), round(oy + y * s, 2)) for x, y in pts]
    out = [f"M{p[0][0]},{p[0][1]}"]
    for i in range(1, len(p), 3):
        a, b, c = p[i], p[i + 1], p[i + 2]
        out.append(f"C{a[0]},{a[1]} {b[0]},{b[1]} {c[0]},{c[1]}")
    return " ".join(out)


def mark_paths(s, ox, oy):
    """A sheet of terms with a signature across it. Mark space is 100 x 100."""
    body = [
        (rounded_rect(22, 16, 78, 84, 6, s, ox, oy), INK),
        (rounded_rect(32, 27, 68, 31.5, 2.2, s, ox, oy), RULE),
        (rounded_rect(32, 37.5, 68, 42, 2.2, s, ox, oy), RULE),
        (rounded_rect(32, 48, 56, 52.5, 2.2, s, ox, oy), RULE),
        (rounded_rect(32, 72, 68, 74.6, 1.3, s, ox, oy), RULE),
    ]
    xml = "".join(
        f'\n    <path\n        android:fillColor="{colour}"\n        android:pathData="{d}" />'
        for d, colour in body
    )
    xml += (
        f'\n    <path\n        android:strokeColor="{COPPER}"'
        f'\n        android:strokeWidth="{round(3.9 * s, 2)}"'
        '\n        android:strokeLineCap="round"'
        '\n        android:strokeLineJoin="round"'
        '\n        android:fillColor="#00000000"'
        f'\n        android:pathData="{signature(s, ox, oy)}" />'
    )
    return xml


# ---------------------------------------------------------------- assets

def launcher_foreground():
    # Adaptive icons only guarantee the central 72dp of the 108dp square, and a circular mask
    # inscribes that. The mark's far corners sit 44.05 units from its centre in mark space, so
    # 0.8 keeps them at 35.2 of the 36 available.
    s, size = 0.8, 80.0
    ox = oy = 54 - size / 2
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        '    android:viewportWidth="108"\n'
        '    android:viewportHeight="108">'
        f"{mark_paths(s, ox, oy)}\n"
        "</vector>\n"
    )


def launcher_background():
    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    xmlns:aapt="http://schemas.android.com/aapt"\n'
        '    android:width="108dp"\n'
        '    android:height="108dp"\n'
        '    android:viewportWidth="108"\n'
        '    android:viewportHeight="108">\n'
        "    <path android:pathData=\"M0,0h108v108h-108z\">\n"
        '        <aapt:attr name="android:fillColor">\n'
        '            <gradient\n'
        '                android:type="linear"\n'
        '                android:startX="0" android:startY="0"\n'
        '                android:endX="108" android:endY="108"\n'
        f'                android:startColor="{PANEL}"\n'
        f'                android:endColor="{DARK}" />\n'
        "        </aapt:attr>\n"
        "    </path>\n"
        "</vector>\n"
    )


def banner(type_):
    w, h = 320.0, 180.0
    # Mark on the left, wordmark on the right.
    mark_size = 100.0
    mark_cx, mark_cy = 62.0, 90.0
    s = mark_size / 100.0
    ox, oy = mark_cx - mark_size / 2, mark_cy - mark_size / 2

    text_x = 106.0
    text_w = 180.0

    # "CONTRACT" fills the text column; "THE" sits above it, smaller and tracked out to about
    # half that width, which is the usual way to stack a two-word wordmark.
    big_track_ratio = 0.02
    big_size = type_.size_to_fit("CONTRACT", text_w, big_track_ratio)
    big_track = big_size * big_track_ratio

    small_size = big_size * 0.5
    small_track = 0.0
    natural = type_.advance("THE", small_size, 0.0)
    target = text_w * 0.46
    if natural < target:
        small_track = (target - natural) / 2.0

    big = type_.path("CONTRACT", big_size, text_x, 104.0, big_track)
    small = type_.path("THE", small_size, text_x, 68.0, small_track)

    return (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n'
        '    xmlns:aapt="http://schemas.android.com/aapt"\n'
        f'    android:width="{int(w)}dp"\n'
        f'    android:height="{int(h)}dp"\n'
        f'    android:viewportWidth="{int(w)}"\n'
        f'    android:viewportHeight="{int(h)}">\n'
        f'    <path android:pathData="M0,0h{int(w)}v{int(h)}h-{int(w)}z">\n'
        '        <aapt:attr name="android:fillColor">\n'
        '            <gradient\n'
        '                android:type="linear"\n'
        '                android:startX="0" android:startY="0"\n'
        f'                android:endX="{int(w)}" android:endY="{int(h)}"\n'
        f'                android:startColor="{PANEL}"\n'
        f'                android:endColor="{DARK}" />\n'
        "        </aapt:attr>\n"
        "    </path>"
        f"{mark_paths(s, ox, oy)}\n"
        f'    <path\n        android:fillColor="{COPPER}"\n        android:pathData="{small}" />\n'
        f'    <path\n        android:fillColor="{INK}"\n        android:pathData="{big}" />\n'
        f'    <path\n        android:fillColor="{COPPER}"\n'
        f'        android:pathData="M{text_x},116h{round(text_w, 2)}v3.4h-{round(text_w, 2)}z" />\n'
        "</vector>\n"
    )


if __name__ == "__main__":
    import sys

    res = sys.argv[1]
    t = Type(FONT)
    open(f"{res}/drawable/ic_launcher_foreground.xml", "w").write(launcher_foreground())
    open(f"{res}/drawable/ic_launcher_background.xml", "w").write(launcher_background())
    open(f"{res}/drawable/tv_banner.xml", "w").write(banner(t))
    print("wrote ic_launcher_foreground.xml, ic_launcher_background.xml, tv_banner.xml")
