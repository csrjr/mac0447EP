import numpy as np
import scipy.io
import cv2
import joblib
import base64
import io
from PIL import Image

from os.path import dirname, join

from sklearn.linear_model import SGDClassifier

H = 256

filename = join(dirname(__file__), "sgd_clf.joblib")
clf = joblib.load(filename)

def get_edges(img):
    return cv2.Canny(img, 20, 230)

def get_seg(edges):
    new_img = np.zeros_like(edges)
    contour, _ = cv2.findContours(edges, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

    for cnt in contour:
        cv2.drawContours(new_img, [cnt], 0, 255, -1)

    rect = cv2.getStructuringElement(cv2.MORPH_RECT, (4, 4))
    dilation = cv2.dilate(new_img, rect, iterations=5)
    erosion = cv2.erode(dilation, rect,  iterations=4)
    dilation = cv2.dilate(erosion, rect, iterations=5)
    erosion = cv2.erode(dilation, rect,  iterations=4)

    #cnt_img = np.zeros_like(erosion)
    #contour, _ = cv2.findContours(erosion, cv2.RETR_CCOMP, cv2.CHAIN_APPROX_SIMPLE)

    #cnt = max(contour, key=cv2.contourArea)
    #cv2.drawContours(cnt_img, [cnt], 0, 255, -1)

    return erosion

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
    seg = get_seg(edges)

    pred = clf.predict([seg.flatten()])[0]

    ratio = np.round(input_img.shape[0] / H)
    box_min, box_max = get_feret_box(seg, 1)

    out_img = draw_feret_box(input_img_resz, box_min, box_max, pred)
    out_img = cv2.cvtColor(out_img, cv2.COLOR_BGR2RGB)

    pil_img = Image.fromarray(out_img)
    buff = io.BytesIO()
    pil_img.save(buff, format="PNG")
    out_str = base64.b64encode(buff.getvalue())
    return "" + str(out_str, 'utf-8')