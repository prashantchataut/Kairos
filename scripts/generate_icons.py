#!/usr/bin/env python3
"""Writes Kairos' launcher icon, notification icon and animated splash from the moment-bloom geometry
(the same maths as ui/art/Bloom.kt), so the icon and the in-app mark are always the same shape."""
import os, sys

RES = sys.argv[1]
CX, CY = 54.0, 50.0
R = 23.0
MOMENT = 1


def petal_len(i):
    return 1.0 if (i % 2 == 0 or i == MOMENT) else 0.8


def petal_path(cx, cy, r, length):
    r0 = r * 0.24
    tip = r0 + (r - r0) * length
    w = r * 0.205
    l = tip - r0
    def p(x, y):
        return f"{cx + x:.2f},{cy + y:.2f}"
    return (
        f"M{p(0, -r0)}"
        f"C{p(w * 0.42, -r0 - l * 0.18)} {p(w, -tip + w * 1.9)} {p(w, -tip + w)}"
        f"C{p(w, -tip + w * 0.45)} {p(w * 0.55, -tip)} {p(0, -tip)}"
        f"C{p(-w * 0.55, -tip)} {p(-w, -tip + w * 0.45)} {p(-w, -tip + w)}"
        f"C{p(-w, -tip + w * 1.9)} {p(-w * 0.42, -r0 - l * 0.18)} {p(0, -r0)}Z"
    )


def circle(cx, cy, r):
    return f"M{cx - r:.2f},{cy:.2f}a{r:.2f},{r:.2f} 0 1,0 {2 * r:.2f},0a{r:.2f},{r:.2f} 0 1,0 {-2 * r:.2f},0"


def cloud(left, bottom, width):
    h = width * 0.42
    base_top = bottom - h * 0.5
    rr = h * 0.25
    parts = [
        f"M{left + rr:.2f},{base_top:.2f}h{width - 2 * rr:.2f}a{rr:.2f},{rr:.2f} 0 0,1 {rr:.2f},{rr:.2f}v{h * 0.5 - 2 * rr:.2f}a{rr:.2f},{rr:.2f} 0 0,1 {-rr:.2f},{rr:.2f}h{-(width - 2 * rr):.2f}a{rr:.2f},{rr:.2f} 0 0,1 {-rr:.2f},{-rr:.2f}v{-(h * 0.5 - 2 * rr):.2f}a{rr:.2f},{rr:.2f} 0 0,1 {rr:.2f},{-rr:.2f}z"
    ]
    for x, y, r in [(0.28, 0.52, 0.22), (0.55, 0.78, 0.3), (0.8, 0.5, 0.2)]:
        parts.append(circle(left + width * x, bottom - h * y, width * r))
    return parts


HEAD = '<vector xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:aapt="http://schemas.android.com/aapt"\n    android:width="108dp" android:height="108dp"\n    android:viewportWidth="108" android:viewportHeight="108">\n'


def grad_linear(x1, y1, x2, y2, stops):
    items = "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops)
    return f'<aapt:attr name="android:fillColor"><gradient android:type="linear" android:startX="{x1}" android:startY="{y1}" android:endX="{x2}" android:endY="{y2}">{items}</gradient></aapt:attr>'


def grad_radial(cx, cy, r, stops):
    items = "".join(f'<item android:offset="{o}" android:color="{c}"/>' for o, c in stops)
    return f'<aapt:attr name="android:fillColor"><gradient android:type="radial" android:centerX="{cx}" android:centerY="{cy}" android:gradientRadius="{r}">{items}</gradient></aapt:attr>'


def petals(shaded=True, mono=None, named=False):
    out = []
    for i in range(8):
        path = petal_path(CX, CY, R, petal_len(i))
        if mono:
            fill = f'android:fillColor="{mono}"'
            inner = f'<path android:pathData="{path}" {fill}/>'
        else:
            if i == MOMENT:
                g = grad_linear(CX, CY - R, CX, CY - R * 0.2, [(0, "#FFFF9C80"), (1, "#FFFF6A4D")])
            else:
                g = grad_linear(CX, CY - R, CX, CY - R * 0.2, [(0, "#FFFFFFFF"), (0.7, "#FFF4F7FF"), (1, "#FFDCE7FF")])
            inner = f'<path android:pathData="{path}">{g}</path>'
        if named:
            out.append(
                f'<group android:name="petal{i}" android:pivotX="{CX}" android:pivotY="{CY}" android:rotation="{i * 45}">'
                f'<group android:name="grow{i}" android:pivotX="{CX}" android:pivotY="{CY}" android:scaleX="0" android:scaleY="0">{inner}</group></group>'
            )
        else:
            out.append(f'<group android:pivotX="{CX}" android:pivotY="{CY}" android:rotation="{i * 45}">{inner}</group>')
    return "\n    ".join(out)


def core(named=False, mono=None):
    cr = R * 0.26
    if mono:
        body = f'<path android:pathData="{circle(CX, CY, cr)}" android:fillColor="{mono}"/>'
    else:
        body = f'<path android:pathData="{circle(CX, CY, cr)}">{grad_radial(CX - cr * 0.3, CY - cr * 0.3, cr * 1.6, [(0, "#FFFFE9A0"), (0.55, "#FFFFC83D"), (1, "#FFFF9A4A")])}</path>'
    if named:
        return f'<group android:name="core" android:pivotX="{CX}" android:pivotY="{CY}" android:scaleX="0" android:scaleY="0">{body}</group>'
    return body


def write(name, text, folder="drawable"):
    d = os.path.join(RES, folder)
    os.makedirs(d, exist_ok=True)
    with open(os.path.join(d, name), "w") as f:
        f.write(text)


# Background: cobalt into sky, with a soft glow where the bloom rises.
write("ic_launcher_background.xml", HEAD +
      f'    <path android:pathData="M0,0h108v108h-108z">{grad_linear(54, 0, 54, 108, [(0, "#FF2F5BFF"), (0.55, "#FF5E9BFF"), (1, "#FFBFDBFF")])}</path>\n'
      f'    <path android:pathData="{circle(CX, CY + 4, 34)}">{grad_radial(CX, CY + 4, 34, [(0, "#73FFFFFF"), (1, "#00FFFFFF")])}</path>\n'
      '</vector>\n')

# Foreground: the bloom rising from a cloud (a sunrise: the right moment).
write("ic_launcher_foreground.xml", HEAD +
      "    " + petals() + "\n    " + core() + "\n"
      + "".join(f'    <path android:pathData="{part}">{grad_linear(54, 68, 54, 90, [(0, "#FFFFFFFF"), (1, "#FFE6EFFF")])}</path>\n' for part in cloud(22, 88, 38) + cloud(46, 90, 40))
      + '</vector>\n')

write("ic_launcher_monochrome.xml", HEAD + "    " + petals(mono="#FF000000") + "\n    " + core(mono="#FF000000") + "\n</vector>\n")

# Notification: the bloom, white, 24dp.
note = petals(mono="#FFFFFFFF") + core(mono="#FFFFFFFF")
write("ic_notification.xml", HEAD.replace('android:width="108dp" android:height="108dp"', 'android:width="24dp" android:height="24dp"').replace('android:viewportWidth="108" android:viewportHeight="108"', f'android:viewportWidth="{2 * R + 6:.0f}" android:viewportHeight="{2 * R + 6:.0f}"') +
      f'    <group android:translateX="{-(CX - R - 3):.2f}" android:translateY="{-(CY - R - 3):.2f}">{note}</group>\n</vector>\n')

# Splash: petals unfold one by one, then the centre pops (the same gesture as the onboarding wake-up).
SPLASH_CY = 54.0
splash_vector = HEAD + f'    <group android:name="all" android:translateY="{SPLASH_CY - CY}">\n    ' + petals(named=True) + "\n    " + core(named=True) + "\n    </group>\n</vector>\n"
targets = []
for i in range(8):
    start = 60 + i * 55
    targets.append(
        f'<target android:name="grow{i}"><aapt:attr name="android:animation"><set>'
        f'<objectAnimator android:propertyName="scaleX" android:valueFrom="0" android:valueTo="1" android:startOffset="{start}" android:duration="420" android:interpolator="@android:interpolator/overshoot"/>'
        f'<objectAnimator android:propertyName="scaleY" android:valueFrom="0" android:valueTo="1" android:startOffset="{start}" android:duration="420" android:interpolator="@android:interpolator/overshoot"/>'
        f'</set></aapt:attr></target>'
    )
    targets.append(
        f'<target android:name="petal{i}"><aapt:attr name="android:animation">'
        f'<objectAnimator android:propertyName="rotation" android:valueFrom="{i * 45 - 28}" android:valueTo="{i * 45}" android:startOffset="{start}" android:duration="480" android:interpolator="@android:interpolator/decelerate_cubic"/>'
        f'</aapt:attr></target>'
    )
targets.append(
    '<target android:name="core"><aapt:attr name="android:animation"><set>'
    '<objectAnimator android:propertyName="scaleX" android:valueFrom="0" android:valueTo="1" android:startOffset="560" android:duration="380" android:interpolator="@android:interpolator/overshoot"/>'
    '<objectAnimator android:propertyName="scaleY" android:valueFrom="0" android:valueTo="1" android:startOffset="560" android:duration="380" android:interpolator="@android:interpolator/overshoot"/>'
    '</set></aapt:attr></target>'
)
write("ic_splash.xml",
      '<animated-vector xmlns:android="http://schemas.android.com/apk/res/android"\n    xmlns:aapt="http://schemas.android.com/aapt">\n'
      '    <aapt:attr name="android:drawable">\n' + splash_vector.replace("\n", "\n        ") + '\n    </aapt:attr>\n    ' +
      "\n    ".join(targets) + "\n</animated-vector>\n")
print("icons written")
