import math, os, random
from PIL import Image, ImageDraw, ImageFilter
RES='/workspace/Kairos/app/src/main/res'
# --- Background: sky gradient + aura + grain, as bitmaps per density ---
def background(px):
    img=Image.new('RGB',(px,px))
    top=(0x24,0x50,0xF0); mid=(0x3E,0x7B,0xFA); low=(0x7F,0xB0,0xFF)
    d=ImageDraw.Draw(img)
    for y in range(px):
        t=y/(px-1)
        if t<0.6: a,b,u=top,mid,t/0.6
        else: a,b,u=mid,low,(t-0.6)/0.4
        d.line([(0,y),(px,y)],fill=tuple(int(a[i]+(b[i]-a[i])*u) for i in range(3)))
    aura=Image.new('RGBA',(px,px),(0,0,0,0)); ad=ImageDraw.Draw(aura)
    for (cx,cy,r,col) in [(0.08,0.12,0.42,(160,140,255,110)),(0.98,0.0,0.38,(255,170,215,95)),(0.8,1.0,0.5,(110,215,255,120)),(0.46,0.54,0.26,(255,255,255,70)),(0.82,0.16,0.2,(255,214,107,120))]:
        ad.ellipse([(cx-r)*px,(cy-r)*px,(cx+r)*px,(cy+r)*px],fill=col)
    aura=aura.filter(ImageFilter.GaussianBlur(px*0.18))
    img=Image.alpha_composite(img.convert('RGBA'),aura)
    rnd=random.Random(7); noise=Image.new('RGBA',(px,px),(0,0,0,0)); npx=noise.load()
    for y in range(px):
        for x in range(px):
            v=rnd.random()
            if v<0.16: npx[x,y]=(0,0,0,int(10+rnd.random()*14))
            elif v>0.86: npx[x,y]=(255,255,255,int(10+rnd.random()*16))
    return Image.alpha_composite(img,noise).convert('RGB')
for dens,mul in [('mdpi',1),('hdpi',1.5),('xhdpi',2),('xxhdpi',3),('xxxhdpi',4)]:
    px=int(108*mul); os.makedirs(f'{RES}/mipmap-{dens}',exist_ok=True)
    background(px).save(f'{RES}/mipmap-{dens}/ic_launcher_background.webp','WEBP',quality=88,method=6)
# --- Foreground: dandelion vector (108dp viewport, mark inside the 66dp safe zone) ---
def mark_paths(cx,cy,r,w,count=12,gap=-52.0):
    lines=[];dots=[]
    for i in range(count):
        deg=i*360/count-90
        dd=(deg-gap+180)%360-180
        if abs(dd)<20: continue
        a=math.radians(deg); L=r*(1 if i%2==0 else 0.82)
        x0,y0=cx+math.cos(a)*r*0.15,cy+math.sin(a)*r*0.15
        x1,y1=cx+math.cos(a)*L,cy+math.sin(a)*L
        lines.append(f"M{x0:.2f},{y0:.2f}L{x1:.2f},{y1:.2f}")
        dots.append((x1,y1,w*0.78))
    dots.append((cx,cy,r*0.14))
    # free seed drifting out of the gap
    ga=math.radians(gap); sx,sy=cx+math.cos(ga)*r*1.5+r*0.15,cy+math.sin(ga)*r*1.5-r*0.12
    ang=ga+0.35; ln=r*0.55
    ux,uy=math.cos(ang),math.sin(ang)
    bx,by=sx-ux*ln*0.5,sy-uy*ln*0.5   # heavy end toward the clock
    tx,ty=sx+ux*ln*0.25,sy+uy*ln*0.25
    lines.append(f"M{bx:.2f},{by:.2f}L{tx:.2f},{ty:.2f}")
    dots.append((bx,by,w*0.9))
    for k in range(-3,4):
        a2=ang+math.radians(k*20); hx,hy=tx+math.cos(a2)*ln*0.42,ty+math.sin(a2)*ln*0.42
        lines.append(f"M{tx:.2f},{ty:.2f}L{hx:.2f},{hy:.2f}")
        dots.append((hx,hy,w*0.45))
    return lines,dots
def circle(x,y,r): return f"M{x-r:.2f},{y:.2f}a{r:.2f},{r:.2f} 0 1,0 {2*r:.2f},0a{r:.2f},{r:.2f} 0 1,0 {-2*r:.2f},0"
def vector(color, cx=50.0, cy=58.0, r=16.5, w=1.9, hair=1.0):
    lines,dots=mark_paths(cx,cy,r,w)
    ray=" ".join(lines[:-8]); seedl=" ".join(lines[-8:-7]); hairs=" ".join(lines[-7:])
    dp=" ".join(circle(*d) for d in dots)
    return f'''<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="108dp" android:height="108dp" android:viewportWidth="108" android:viewportHeight="108">
    <path android:pathData="{ray}" android:strokeColor="{color}" android:strokeWidth="{w}" android:strokeLineCap="round"/>
    <path android:pathData="{seedl}" android:strokeColor="{color}" android:strokeWidth="{w*0.9:.2f}" android:strokeLineCap="round"/>
    <path android:pathData="{hairs}" android:strokeColor="{color}" android:strokeWidth="{hair}" android:strokeLineCap="round"/>
    <path android:pathData="{dp}" android:fillColor="{color}"/>
</vector>
'''
open(f'{RES}/drawable/ic_launcher_foreground.xml','w').write(vector('#FFFFFFFF'))
open(f'{RES}/drawable/ic_launcher_monochrome.xml','w').write(vector('#FFFFFFFF'))
open(f'{RES}/drawable/ic_splash.xml','w').write(vector('#FF2340E8'))
# notification icon: 24dp, simplified 8 rays
open(f'{RES}/drawable/ic_notification.xml','w').write(vector('#FFFFFFFF',cx=50,cy=58,r=30,w=5.5,hair=2.8).replace('android:width="108dp" android:height="108dp"','android:width="24dp" android:height="24dp"'))
# preview png for review
