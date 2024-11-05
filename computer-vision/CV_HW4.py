from imageai.Detection import ObjectDetection
import os
import csv
import cv2

yolo_v3_model_path = "./models/yolov3.pt"
csv_filename = "output_results.csv"
identifier = 0
detector = ObjectDetection()

detector.setModelTypeAsYOLOv3()
detector.setModelPath(yolo_v3_model_path)
detector.loadModel()


def delete_file(file_path):
    if os.path.exists(file_path):
        os.remove(file_path)


def detect_objects(image):
    _, detections = detector.detectObjectsFromImage(
        input_image=image,
        output_type='array',
    )

    return detections


def write_to_csv(content):
    with open(csv_filename, 'a', newline='') as csvfile:
        csv_writer = csv.writer(csvfile)
        csv_writer.writerow(content)


delete_file(csv_filename)

cap = cv2.VideoCapture(0)

while True:
    ret, frame = cap.read()

    cv2.imshow('frame', frame)

    detected_objects = detect_objects(frame)

    if len(detected_objects):
        for index, detected_object in enumerate(detected_objects):
            write_to_csv([identifier, index, detected_object['name'], detected_object['box_points']])

    identifier += 1

    if cv2.waitKey(1) & 0xFF == ord('q'):
        break


cap.release()
cv2.destroyAllWindows()
