from PIL import Image, ImageEnhance
from math import sqrt

lego_colors = [ (255,255,255), # white
                (0,0,0), #black
                (212,5,38), #red 1
                (130,3,23), #red 2
                (104,230,0), #green 1
                (0,133,40), #green 2
                (0,82,24), #green 3
                (190,198,207), #gray 1
                (119,124,130), #gray 2
                (245,241,0), #yellow
                (95,0,158), #purple
                (242,236,199), #beige
                (255,114,0), #orange
                (110,69,47), #brown
                (179,0,158), #pink 1
                (239,161,255), #pink 2
                (29,35,99), #blue 1
                (36,46,212), #blue 2
                (0,206,255) #blue 3
                ]

ok_color = [False for _ in range(len(lego_colors))]


def dist(pixel1, pixel2):
    r, g, b = pixel1
    r2,g2,b2 = pixel2
    return abs(r-r2) + abs(g-g2) + abs(b-b2)

def closest_color(pixel, palette):
    assert len(palette)>0
    min_index, min_dist = 0, dist(pixel, palette[0])
    for k in range(1, len(palette)):
        color = palette[k]
        d = dist(pixel, color)
        if d < min_dist:
            min_index = k; min_dist = d
    ok_color[min_index] = True
    return palette[min_index]

def transform(path, savePath, palette):
    im = Image.open(path)
    image = im.load()
    color_count = len(palette)

    width, height = im.size
    res_im = Image.new(mode="RGB", size = (width, height))
    res = res_im.load()
    for x in range(width):
        print(f"row {x} / {width}")
        for y in range(height):
            res[x, y] = closest_color(image[x, y], palette)
    res_im.show()

    text = path.split(".")
    filename = f"{text[0]}-colors={color_count}.{text[1]}"
    res_im.save(savePath + f"{text[0]}-colors={color_count}.{text[1]}")
    return filename

def to_pixels(image, savePath, height = 80, sat = 1):
    img = Image.open(image)
    i_size = img.size

    width = int(i_size[0]*height/i_size[1])
    res=img.resize((width, height),Image.BILINEAR)

    filename=f'{image.split(".")[0]}_{i_size[0]}x{i_size[1]}.png'
    converter = ImageEnhance.Color(res)
    res = converter.enhance(sat)
    res.save(savePath+filename)
    #res.show()
    return filename

def color_index(palette, color):
    for k in range(len(palette)):
        if color == palette[k]:
            return k
    raise ValueError

def convert_to_array(filename, savePath, im, image):
    width, height = im.size
    file = open(savePath + filename + ".txt", 'w')
    for x in range(width):
        for y in range(height):
            r, g, b = image[x, y]
            tmp = "|" if y else ""
            file.write(f'{tmp}0,{r},{g},{b}')
        file.write('\n' if x != width-1 else "")
    file.close()


def legofy(filename, savePath, height=80, saturation=4):

    file = open(savePath+"/test.txt", "w")
    file.write('aaa')
    file.close()

    filename = transform(to_pixels(filename, savePath, height=80, sat = saturation), savePath, lego_colors)
    im = Image.open(filename)
    image = im.load()

    convert_to_array(filename, savePath, im, image)
