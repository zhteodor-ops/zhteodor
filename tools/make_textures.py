"""Paints the Jerry Mod textures (original designs). Run: python tools/make_textures.py"""
import random
from PIL import Image

OUT = "src/main/resources/assets/jerrymod/textures/entity/"
random.seed(7)


def faces(u, v, w, h, d):
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def jitter(c, amt):
    return tuple(max(0, min(255, x + random.randint(-amt, amt))) for x in c) + (255,)


def fill_box(img, u, v, w, h, d, color, amt=12, face_colors=None):
    for name, (x, y, fw, fh) in faces(u, v, w, h, d).items():
        col = (face_colors or {}).get(name, color)
        for i in range(fw):
            for j in range(fh):
                img.putpixel((x + i, y + j), jitter(col, amt))


def px(img, face, u, v, w, h, d, x, y, color):
    fx, fy, fw, fh = faces(u, v, w, h, d)[face]
    if 0 <= x < fw and 0 <= y < fh:
        img.putpixel((fx + x, fy + y), color + (255,) if len(color) == 3 else color)


# ---------------- Normal Jerry: tan & cream mutt, dark saddle, amber eyes ----------------
def dog():
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    tan, cream, dark = (196, 150, 96), (236, 222, 196), (92, 66, 44)
    fill_box(img, 0, 0, 6, 6, 10, tan, face_colors={"top": dark, "bottom": cream})   # body
    fill_box(img, 32, 0, 6, 6, 5, tan, face_colors={"top": tan})                    # head
    fill_box(img, 0, 16, 3, 3, 3, cream)                                            # snout
    fill_box(img, 12, 16, 2, 3, 1, dark)                                            # ears
    fill_box(img, 18, 16, 2, 3, 1, dark)
    fill_box(img, 24, 16, 2, 7, 2, tan)                                             # legs
    for f in ("front", "back", "left", "right"):                                    # white socks
        fx, fy, fw, fh = faces(24, 16, 2, 7, 2)[f]
        for i in range(fw):
            for j in range(fh - 2, fh):
                img.putpixel((fx + i, fy + j), jitter(cream, 6))
    fill_box(img, 32, 16, 2, 7, 2, tan)                                             # tail
    fx, fy, fw, fh = faces(32, 16, 2, 7, 2)["front"]
    for i in range(fw):
        img.putpixel((fx + i, fy + fh - 1), jitter(cream, 4))
    # face: cream muzzle patch + amber eyes
    for x in range(1, 5):
        for y in range(3, 6):
            px(img, "front", 32, 0, 6, 6, 5, x, y, jitter(cream, 6)[:3])
    for x in (1, 4):
        px(img, "front", 32, 0, 6, 6, 5, x, 2, (40, 28, 18))
    px(img, "front", 32, 0, 6, 6, 5, 1, 1, (210, 150, 40))
    px(img, "front", 32, 0, 6, 6, 5, 4, 1, (210, 150, 40))
    px(img, "front", 32, 0, 6, 6, 5, 2, 2, (250, 250, 250))
    px(img, "front", 32, 0, 6, 6, 5, 3, 2, (250, 250, 250))
    # nose
    for x in range(3):
        px(img, "front", 0, 16, 3, 3, 3, x, 0, (30, 22, 18))
    px(img, "top", 0, 16, 3, 3, 3, 1, 2, (30, 22, 18))
    img.save(OUT + "jerry_dog.png")


# ---------------- Demonic Jerry: charred hide, glowing cracks, bone horns ----------------
def demon():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    hide, flesh, bone, black = (38, 30, 32), (120, 22, 26), (214, 204, 178), (14, 10, 10)
    fill_box(img, 0, 0, 10, 10, 16, hide, 10)
    fill_box(img, 52, 0, 8, 8, 7, hide, 10)
    fill_box(img, 0, 26, 6, 4, 5, hide, 10, face_colors={"bottom": flesh})
    fill_box(img, 22, 26, 6, 2, 6, hide, 10, face_colors={"top": flesh})
    fill_box(img, 46, 26, 2, 5, 2, bone, 8)
    fill_box(img, 54, 26, 2, 5, 2, bone, 8)
    fill_box(img, 62, 26, 2, 4, 2, bone, 8)
    fill_box(img, 0, 40, 4, 9, 4, hide, 10)
    fill_box(img, 16, 40, 3, 10, 3, hide, 10)
    # claws
    for f in ("front", "left", "right", "back"):
        fx, fy, fw, fh = faces(0, 40, 4, 9, 4)[f]
        for i in range(0, fw, 2):
            img.putpixel((fx + i, fy + fh - 1), bone + (255,))
    # glowing lava cracks on body + legs (also in eye layer so they glow)
    for (u, v, w, h, d) in ((0, 0, 10, 10, 16), (0, 40, 4, 9, 4), (52, 0, 8, 8, 7)):
        for name, (x, y, fw, fh) in faces(u, v, w, h, d).items():
            for _ in range(max(1, fw * fh // 14)):
                cx, cy = x + random.randrange(fw), y + random.randrange(fh)
                col = random.choice([(255, 80, 20), (220, 40, 10), (255, 140, 40)])
                img.putpixel((cx, cy), col + (255,))
                eyes.putpixel((cx, cy), col + (150,))
    # red eyes
    for x in (1, 2, 5, 6):
        for y in (3,):
            px(img, "front", 52, 0, 8, 8, 7, x, y, (255, 30, 20))
            px(eyes, "front", 52, 0, 8, 8, 7, x, y, (255, 40, 20))
    px(img, "front", 52, 0, 8, 8, 7, 2, 2, black)
    px(img, "front", 52, 0, 8, 8, 7, 5, 2, black)
    # teeth on snout front + bottom, and on jaw
    for x in range(6):
        px(img, "front", 0, 26, 6, 4, 5, x, 3, bone if x % 2 == 0 else flesh)
    for x in range(6):
        for y in range(5):
            if y in (0, 4) or x in (0, 5):
                px(img, "bottom", 0, 26, 6, 4, 5, x, y, bone)
    for x in range(6):
        px(img, "front", 22, 26, 6, 2, 6, x, 0, bone if x % 2 == 1 else flesh)
        for y in range(6):
            if x in (0, 5):
                px(img, "top", 22, 26, 6, 2, 6, x, y, bone)
    img.save(OUT + "jerry_demon.png")
    eyes.save(OUT + "jerry_demon_eyes.png")


# ---------------- Skinwalker Jerry: grey hairless skin, ribs, dog skull ----------------
def skinwalker():
    img = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    eyes = Image.new("RGBA", (128, 64), (0, 0, 0, 0))
    skin, rib, bone, sock = (112, 112, 102), (72, 70, 64), (222, 216, 198), (12, 10, 10)
    fill_box(img, 0, 0, 8, 16, 5, skin, 10)
    # ribs on chest and back
    for face in ("front", "back"):
        for y in range(3, 11, 2):
            for x in range(8):
                if x not in (3, 4) or face == "back":
                    px(img, face, 0, 0, 8, 16, 5, x, y, jitter(rib, 5)[:3])
        for y in range(1, 15):
            px(img, face, 0, 0, 8, 16, 5, 3 if face == "front" else 4, y, jitter(rib, 4)[:3])
    fill_box(img, 26, 0, 6, 6, 6, bone, 8)
    fill_box(img, 50, 0, 4, 3, 4, bone, 8)
    fill_box(img, 66, 0, 1, 3, 1, skin, 8)
    fill_box(img, 70, 0, 1, 3, 1, skin, 8)
    # skull: eye sockets with tiny white pupils
    for x in (0, 1, 4, 5):
        for y in (2, 3):
            px(img, "front", 26, 0, 6, 6, 6, x, y, sock)
    px(img, "front", 26, 0, 6, 6, 6, 1, 3, (255, 255, 255))
    px(img, "front", 26, 0, 6, 6, 6, 4, 3, (255, 255, 255))
    px(eyes, "front", 26, 0, 6, 6, 6, 1, 3, (255, 255, 255))
    px(eyes, "front", 26, 0, 6, 6, 6, 4, 3, (255, 255, 255))
    # skull cracks
    for (x, y) in ((2, 0), (2, 1), (3, 1)):
        px(img, "front", 26, 0, 6, 6, 6, x, y, rib)
    # snout: nose hole + teeth
    px(img, "front", 50, 0, 4, 3, 4, 1, 0, sock)
    px(img, "front", 50, 0, 4, 3, 4, 2, 0, sock)
    for x in range(4):
        px(img, "front", 50, 0, 4, 3, 4, x, 2, (250, 248, 235) if x % 2 == 0 else sock)
    fill_box(img, 0, 40, 3, 20, 3, skin, 10)
    # knobby knees
    for face in ("front",):
        px(img, face, 0, 40, 3, 20, 3, 1, 9, rib)
        px(img, face, 0, 40, 3, 20, 3, 1, 10, rib)
    fill_box(img, 24, 40, 2, 22, 2, skin, 10)
    fill_box(img, 40, 40, 3, 3, 3, sock, 6)   # black claws
    for face in ("front", "bottom"):
        for x in range(3):
            px(img, face, 40, 40, 3, 3, 3, x, 2, bone)
    img.save(OUT + "jerry_skinwalker.png")
    eyes.save(OUT + "jerry_skinwalker_eyes.png")


dog()
demon()
skinwalker()
print("textures written")
