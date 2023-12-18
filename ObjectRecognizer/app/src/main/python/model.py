import numpy as np
import scipy.io
import cv2
import joblib
import base64
import io
from PIL import Image

from os.path import dirname, join

from sklearn.tree import DecisionTreeClassifier

H = 256

filename = join(dirname(__file__), "clf.joblib")
clf = joblib.load(filename)

clahe = cv2.createCLAHE()


def normalize(img):
    return clahe.apply(img)


def binarize(img):
    _, thresh = cv2.threshold(cv2.GaussianBlur(img, (5,5), 0), 0, 255, cv2.THRESH_BINARY+cv2.THRESH_OTSU)
    return thresh


def get_edges(img):
    blur = cv2.GaussianBlur(img, (3,3), 0)
    return cv2.Canny(blur, 20, 255)


def get_seg(img):
    contour, _ = cv2.findContours(img, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

    for cnt in contour:
        cv2.drawContours(img, [cnt], 0, 255, -1)

    rect = cv2.getStructuringElement(cv2.MORPH_RECT, (3, 3))
    dilation = cv2.dilate(img, rect, iterations=2)
    closing = cv2.morphologyEx(dilation, cv2.MORPH_CLOSE, rect)

    final_cnts, _ = cv2.findContours(closing, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)
    cnt = max(final_cnts, key=cv2.contourArea)

    seg = np.zeros_like(img)
    cv2.drawContours(seg, [cnt], 0, 255, -1)

    return seg, cnt


def get_features(cnt):
    area = cv2.contourArea(cnt)

    M = cv2.moments(cnt)
    cx = int(M["m10"] / M["m00"])
    cy = int(M["m01"] / M["m00"])

    rect = cv2.minAreaRect(cnt)
    box = cv2.boxPoints(rect)
    box = np.intp(box)
    area_norm_box = area/cv2.contourArea(box)

    (x,y), r = cv2.minEnclosingCircle(cnt)
    area_norm_circle = area/(np.pi * r**2)

    cx_norm = abs(cx - x)
    cy_norm = abs(cy - y)

    w, h = rect[1]
    stretch = max(w/h, h/w)

    return area_norm_box, area_norm_circle, cx_norm, cy_norm, stretch


def get_feret_box(img_seg, ratio):
    x_in, y_in =np.where(img_seg == 255)

    box_min = (int(min(y_in)*ratio), int(min(x_in)*ratio))
    box_max = (int(max(y_in)*ratio), int(max(x_in)*ratio))

    return box_min, box_max


def draw_feret_box(img, box_min, box_max, txt):
    img_copy = img.copy()

    cv2.rectangle(img_copy, box_min, box_max, (0,0,255), thickness=2)
    cv2.putText(img_copy, txt, (box_min[0], box_min[1]-5), cv2.FONT_HERSHEY_SIMPLEX, 0.5, (0,0,255), 1)

    return img_copy


def detect_and_draw(img_str):
    decoded_data = base64.b64decode(img_str)
    np_data = np.fromstring(decoded_data, np.uint8)
    input_img = cv2.imdecode(np_data, cv2.IMREAD_UNCHANGED)

    input_img_resz = cv2.resize(input_img, (H,H))
    img = cv2.cvtColor(input_img_resz, cv2.COLOR_BGR2GRAY)

    edges = get_edges(img)
    seg, cnt = get_seg(edges)

    feats = get_features(cnt)

    pred = clf.predict([feats])[0]

    ratio = np.round(input_img.shape[0] / H)
    box_min, box_max = get_feret_box(seg, 1)

    out_img = draw_feret_box(input_img_resz, box_min, box_max, pred)
    out_img = cv2.cvtColor(out_img, cv2.COLOR_BGR2RGB)

    pil_img = Image.fromarray(out_img)
    buff = io.BytesIO()
    pil_img.save(buff, format="PNG")
    out_str = base64.b64encode(buff.getvalue())
    return "" + str(out_str, 'utf-8')