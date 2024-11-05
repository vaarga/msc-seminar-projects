import cv2
import os
import joblib
import numpy as np
from sklearn.model_selection import train_test_split
from sklearn.svm import LinearSVC
from sklearn.metrics import accuracy_score, confusion_matrix
from skimage.transform import pyramid_gaussian
import matplotlib.pyplot as plt
from imutils.object_detection import non_max_suppression


# The code for the following function was fully borrowed from:
# https://github.com/BUPTLdy/human-detector/blob/master/object_detector/detector.py
def sliding_window(image, window_size, step_size):
    """
    This function returns a patch of the input 'image' of size
    equal to 'window_size'. The first image returned top-left
    co-ordinate (0, 0) and are increment in both x and y directions
    by the 'step_size' supplied.

    So, the input parameters are-
    image - Input image
    window_size - Size of Sliding Window
    step_size - incremented Size of Window

    The function returns a tuple -
    (x, y, im_window)
    """
    for y in range(0, image.shape[0], step_size[1]):
        for x in range(0, image.shape[1], step_size[0]):
            yield x, y, image[y: y + window_size[1], x: x + window_size[0]]


class HoGProcessor:
    def __init__(self, neg_data_path, pos_data_path, block_size, cell_size, nr_of_bins):
        self.neg_data_path = neg_data_path
        self.pos_data_path = pos_data_path
        self.block_size = block_size
        self.cell_size = cell_size
        self.nr_of_bins = nr_of_bins
        self.model = None

    def compute_hog(self, input_image):
        if input_image.ndim == 3:
            if input_image.dtype == np.float64:
                input_image = (input_image * 255).astype(np.uint8)

            input_image = cv2.cvtColor(input_image, cv2.COLOR_BGR2GRAY)
        elif input_image.ndim == 2:
            if input_image.dtype == np.float64:
                input_image = (input_image * 255).astype(np.uint8)
        else:
            raise ValueError("Unsupported image depth")

        hogDesc = cv2.HOGDescriptor(_winSize=(64, 64), _blockSize=self.block_size,
                                    _blockStride=self.cell_size, _cellSize=self.cell_size,
                                    _nbins=self.nr_of_bins)

        return hogDesc.compute(input_image)

    def generate_features(self, path_to_folder):
        hog_features = []

        for image_filename in os.listdir(path_to_folder):
            image = cv2.imread(os.path.join(path_to_folder, image_filename))

            hog_features.append(self.compute_hog(image))

        return np.array(hog_features)

    def train_svm(self, neg_data, pos_data):
        model_path = "./models/svm.model"

        if os.path.exists(model_path):
            print("Loading existing model...")

            self.model = joblib.load(model_path)
        else:
            neg_classes = [0] * len(neg_data)
            pos_classes = [1] * len(pos_data)

            X = np.concatenate((neg_data, pos_data), axis=0)
            y = np.append(neg_classes, pos_classes)

            X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, shuffle=True)

            svm = LinearSVC(dual='auto')

            svm.fit(X_train, y_train)

            y_predicted = svm.predict(X_test)

            print('Accuracy: ', accuracy_score(y_test, y_predicted))
            print(confusion_matrix(y_test, y_predicted))

            self.model = svm

            print("Saving the model...")

            joblib.dump(self.model, model_path)

    # The code for the following method was partially borrowed from:
    # https://github.com/BUPTLdy/human-detector/blob/master/object_detector/detector.py
    def predict(self, image):
        min_wdw_sz = (64, 128)
        step_size = (10, 10)
        downscale = 1.25
        detections = []

        for im_scaled in pyramid_gaussian(image, downscale=downscale):
            if im_scaled.shape[0] < min_wdw_sz[1] or im_scaled.shape[1] < min_wdw_sz[0]:
                break

            for (x, y, im_window) in sliding_window(im_scaled, min_wdw_sz, step_size):
                if im_window.shape[0] != min_wdw_sz[1] or im_window.shape[1] != min_wdw_sz[0]:
                    continue

                hog_features = self.compute_hog(im_window)

                hog_features = hog_features.reshape(1, -1)
                pred = self.model.predict(hog_features)

                if pred == 1 and self.model.decision_function(hog_features) > 0.5:
                    detections.append((int(x * downscale), int(y * downscale),
                                       self.model.decision_function(hog_features),
                                       int(min_wdw_sz[0] * downscale),
                                       int(min_wdw_sz[1] * downscale)))

        rects = np.array([[x, y, x + w, y + h] for (x, y, _, w, h) in detections])
        scores = [score[0] for (x, y, score, w, h) in detections]
        pick = non_max_suppression(rects, probs=scores, overlapThresh=0.3)

        for (xA, yA, xB, yB) in pick:
            cv2.rectangle(image, (xA, yA), (xB, yB), (0, 255, 0), thickness=2)

        plt.axis("off")
        plt.imshow(cv2.cvtColor(image, cv2.COLOR_BGR2RGB))
        plt.title("Final Detections after applying NMS")
        plt.show()


NEG_FOLDER_PATH = './neg_person'
POS_FOLDER_PATH = './pos_person'

HoGP = HoGProcessor(NEG_FOLDER_PATH, POS_FOLDER_PATH, (16, 16), (8, 8), 9)

neg_features = HoGP.generate_features(NEG_FOLDER_PATH)
pos_features = HoGP.generate_features(POS_FOLDER_PATH)

HoGP.train_svm(neg_features, pos_features)

test_images_folder = './test_image/'

for filename in os.listdir(test_images_folder):
    if filename.lower().endswith(('.png', '.jpg')):
        image_path = os.path.join(test_images_folder, filename)
        pos_image = cv2.imread(image_path)
        pos_image_features = HoGP.compute_hog(pos_image)

        HoGP.predict(pos_image)

cv2.waitKey(0)
cv2.destroyAllWindows()
