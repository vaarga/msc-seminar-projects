import cv2
import numpy as np


def apply_homomorphic_filtering(d0, gamma_h, gamma_l, img):
    m, n, c = img.shape

    if c > 1:
        img = cv2.cvtColor(img, cv2.COLOR_BGR2GRAY)

    G = np.zeros((m, n))
    x0 = m // 2
    y0 = n // 2
    f = np.fft.fft2(img)
    f_shift = np.fft.fftshift(f)
    c = 0.5

    for i in range(-(m - 1) // 2, (m - 1) // 2):
        for j in range(-(n - 1) // 2, (n - 1) // 2):
            x = i + x0
            y = j + y0
            dist = ((x - x0) ** 2 + (y - y0) ** 2) ** 0.5
            G[x, y] = ((gamma_h - gamma_l) * (1 - np.exp(-c * ((dist / d0) ** 2)))) + gamma_l

    filtered_image = np.multiply(f_shift, G)
    filtered_image = np.fft.ifftshift(filtered_image)
    filtered_image = np.fft.ifft2(filtered_image)
    filtered_image = np.abs(filtered_image)
    filtered_image = np.array(filtered_image, np.uint8)

    return filtered_image


# Image
image = cv2.imread("city_hall.jpg")

filtered_img = apply_homomorphic_filtering(30, 2, 0.5, image)

cv2.imshow("Homomorphic Filtered Frame (Image)", filtered_img)

# Camera
cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()
    filtered_video = apply_homomorphic_filtering(30, 2, 0.5, frame)

    cv2.imshow("Homomorphic Filtered Frame (Camera)", filtered_video)

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break

cap.release()
cv2.destroyAllWindows()
