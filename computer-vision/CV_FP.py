import cv2
import os

FACE_MIN_SIZE = (24, 24)
FACE_MAX_SIZE = (256, 256)
FACE_DESIRED_SIZE = (50, 50)

# If it is not equal to 0, it will resize the original video before attempting to detect faces
VIDEO_RESIZE = 0.2

INPUT_DIRECTORY = 'input_videos'
OUTPUT_DIRECTORY = 'output_detected_faces'
VIDEO_FILE_NAME = 'test2'
VIDEO_FILE_EXTENSION = 'mp4'
CLASSIFIER_NAME = cv2.data.haarcascades + "haarcascade_frontalface_default.xml"

# If set to 'True', it will save all the detected faces (resized) to the desired output directory
SAVE_FACES = True

# If set to 'True', it will display the original video with detected faces (highlighted by a green box around them) as
# it iterates through the video
SHOW_FACES_REAL_TIME = False

classifier = cv2.CascadeClassifier(CLASSIFIER_NAME)


def create_folder(folder_path):
    if not os.path.exists(folder_path):
        os.makedirs(folder_path)


def detect_faces(face_classifier, frame):
    frame_in_grayscale = cv2.cvtColor(frame, cv2.COLOR_RGB2GRAY)

    return face_classifier.detectMultiScale(
        frame_in_grayscale, scaleFactor=1.05, minNeighbors=20, minSize=FACE_MIN_SIZE, maxSize=FACE_MAX_SIZE
    )


def save_face(frame, x, y, w, h, frame_nr, face_nr):
    face = frame[y:y+h, x:x+w]

    resized_face = cv2.resize(face, FACE_DESIRED_SIZE, interpolation=cv2.INTER_AREA)

    cv2.imwrite(f"{OUTPUT_DIRECTORY}/{VIDEO_FILE_NAME}_{frame_nr}_{face_nr}.jpg", resized_face)


def draw_rectangles_around_faces(frame, x, y, w, h):
    cv2.rectangle(frame, (x, y), (x + w, y + h), (0, 255, 0), 4)


def iterate_faces(frame, faces, frame_nr, save_faces, show_faces_real_time):
    for face_nr, (x, y, w, h) in enumerate(faces):
        if save_faces:
            save_face(frame, x, y, w, h, frame_nr, face_nr)

        if show_faces_real_time:
            draw_rectangles_around_faces(frame, x, y, w, h)

            cv2.imshow('Face(s) detection', frame)


create_folder(OUTPUT_DIRECTORY)

video_full_path = f"{INPUT_DIRECTORY}/{VIDEO_FILE_NAME}.{VIDEO_FILE_EXTENSION}"

cap = cv2.VideoCapture(video_full_path)

frame_counter = 0

while cap.isOpened():
    _, frame = cap.read()

    if (frame is None) or (cv2.waitKey(25) & 0xFF == ord('q')):
        break

    if VIDEO_RESIZE:
        frame = cv2.resize(frame, (0, 0), fx=VIDEO_RESIZE, fy=VIDEO_RESIZE)

    faces = detect_faces(classifier, frame)

    iterate_faces(frame, faces, frame_counter, SAVE_FACES, SHOW_FACES_REAL_TIME)

    frame_counter += 1

cap.release()

cv2.destroyAllWindows()
