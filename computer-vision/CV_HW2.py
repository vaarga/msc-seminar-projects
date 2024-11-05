import cv2
import math
import numpy as np
from sklearn.linear_model import LogisticRegression

skin_pixels = {
    'r': [],
    'g': [],
    'b': [],
}


def classify_pixel(original_r, original_g, original_b):
    r, g, b = int(original_r), int(original_g), int(original_b)

    if (
            (r > 95) and (g > 40) and (b > 20) and
            ((max([r, g, b]) - min([r, g, b])) > 15) and
            (abs(r - g) > 15) and (r > g) and (r > b)
    ):
        return 0

    skin_pixels['r'].append(r)
    skin_pixels['g'].append(g)
    skin_pixels['b'].append(b)

    return 255


def detect_skin(rgb_image):
    (r, g, b) = cv2.split(rgb_image)

    rows_nr = len(r)
    columns_nr = len(r[0])

    mask = np.zeros((rows_nr, columns_nr), dtype=np.uint8)

    for i in range(rows_nr):
        for j in range(columns_nr):
            mask[i, j] = classify_pixel(r[i, j], g[i, j], b[i, j])

    cv2.imshow("Mask (1)", mask)

    return cv2.merge([mask, mask, mask])


def gauss_probability(r, g, b):
    sigma = np.matrix(np.cov(np.vstack([r, g, b])))

    mu = np.array([
        np.mean(r),
        np.mean(g),
        np.mean(b),
    ])

    return sigma, mu


def compute_gauss(x, mu, sigma):
    size = len(x)

    if size == len(mu) and (size, size) == sigma.shape:
        det = np.linalg.det(sigma)

        if det == 0:
            raise NameError("The covariance matrix can't be singular")

        norm_const = 1.0 / (math.pow((2 * math.pi), float(size) / 2) * math.pow(det, 1.0 / 2))
        x_mu = np.matrix(x - mu)
        inv = sigma.I
        # result = math.pow(math.e, -0.5 * (x_mu * inv * x_mu.T))
        result = np.exp(-0.5 * (x_mu * inv * x_mu.T))[0, 0]

        return norm_const * result
    else:
        raise NameError("The dimensions of the input don't match")


def gauss_face_detect(rgb_image):
    (r, g, b) = cv2.split(rgb_image)

    rows_nr = len(r)
    columns_nr = len(r[0])

    mask = np.zeros((rows_nr, columns_nr), dtype=np.float64)
    mask_threshold = np.zeros((rows_nr, columns_nr), dtype=np.uint8)

    # In order to make the 'gauss_probability' function work correctly we have to
    # run prior to this the 'detect_skin' function on the same image
    sigma, mu = gauss_probability(skin_pixels['r'], skin_pixels['g'], skin_pixels['b'])
    threshold = 0.0000002

    for i in range(rows_nr):
        for j in range(columns_nr):
            computed_gauss = compute_gauss(np.array([r[i, j], g[i, j], b[i, j]]), mu, sigma)
            mask[i, j] = computed_gauss
            mask_threshold[i, j] = 255 if computed_gauss > threshold else 0

    cv2.normalize(mask, mask, 0, 255, cv2.NORM_MINMAX)

    mask = mask.astype(np.uint8)

    cv2.imshow("Mask - Norm Min Max (2)", mask)
    cv2.imshow("Mask - Threshold (2)", mask_threshold)

    return cv2.merge([mask, mask, mask]), cv2.merge([mask_threshold, mask_threshold, mask_threshold])


def get_training_data(rgb_image):
    X_train_local = rgb_image.reshape((-1, 3))
    y_train_local = []

    for pixel in X_train_local:
        y_train_local.append(classify_pixel(pixel[0], pixel[1], pixel[2]) / 255)

    return X_train_local, np.array(y_train_local)


def log_reg_face_detect(rgb_image, regression_model):
    X_test = rgb_image.reshape((-1, 3))

    y_predict = regression_model.predict(X_test) * 255

    mask = y_predict.reshape(rgb_image.shape[0], rgb_image.shape[1], 1).astype(np.uint8)

    cv2.imshow("Mask - Logistic Regression (3)", mask)

    return cv2.merge([mask, mask, mask])


# Testing the "detect_skin" function
frame = cv2.imread('faces.png')

cv2.imshow("Original Image (1)", frame)

skin_mask = detect_skin(frame)

skin = cv2.bitwise_and(frame, skin_mask)

cv2.imshow("Masked Image - Detect Skin (1)", skin)

cv2.waitKey(0)
cv2.destroyAllWindows()

# Testing the "gauss_face_detect" and "compute_gauss" functions
frame = cv2.imread('faces.png')

cv2.imshow("Original Image (2)", frame)

skin_mask, skin_mask_threshold = gauss_face_detect(frame)

skin = cv2.bitwise_and(frame, skin_mask)

cv2.imshow("Masked Image - Gauss Face Detect Norm Min Max (2)", skin)

skin = cv2.bitwise_and(frame, skin_mask_threshold)

cv2.imshow("Masked Image - Gauss Face Detect Threshold (2)", skin)

cv2.waitKey(0)
cv2.destroyAllWindows()

# Testing the "log_reg_face_detect" function
logistic_regression = LogisticRegression()

frame = cv2.imread('faces.png')

cv2.imshow("Original Image (3)", frame)

X_train, y_train = get_training_data(frame)

logistic_regression.fit(X_train, y_train)

skin_mask = log_reg_face_detect(frame, logistic_regression)

skin = cv2.bitwise_and(frame, skin_mask)

cv2.imshow("Masked Image - Logistic Regression (3)", skin)

cv2.waitKey(0)
cv2.destroyAllWindows()
