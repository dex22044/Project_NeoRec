from json import JSONEncoder
import struct
import face_recognition
import cv2
import numpy as np
import os
from http.server import BaseHTTPRequestHandler
from http.server import HTTPServer

known_face_encodings = []
known_face_names = []

def addFace(id, enc):
    known_face_names.append(str(id))
    known_face_encodings.append(enc)

def process_1(serv : BaseHTTPRequestHandler):
    content_length = int(serv.headers['Content-Length'])
    post_data = serv.rfile.read(content_length)
    frame = cv2.imdecode(np.fromstring(post_data, np.uint8), cv2.IMREAD_COLOR)

    face_locations = []
    face_encodings = []
    face_names = []
    small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)

    rgb_small_frame = small_frame[:, :, ::-1]
    
    face_locations = face_recognition.face_locations(small_frame)
    face_encodings = face_recognition.face_encodings(small_frame, face_locations)

    face_names = []
    for face_encoding in face_encodings:
        name = "Unknown"
        if len(known_face_encodings) > 0:
            matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
            face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
            best_match_index = np.argmin(face_distances)
            if matches[best_match_index]:
                name = known_face_names[best_match_index]

        face_names.append(name)

    for (top, right, bottom, left), name in zip(face_locations, face_names):
        top *= 4
        right *= 4
        bottom *= 4
        left *= 4

        cv2.rectangle(frame, (left, top), (right, bottom), (0, 0, 255), 2)
        cv2.rectangle(frame, (left, bottom - 35), (right, bottom), (0, 0, 255), cv2.FILLED)
        font = cv2.FONT_HERSHEY_DUPLEX
        cv2.putText(frame, name, (left + 6, bottom - 6), font, 1.0, (255, 255, 255), 1)
    
    img_data_ret = cv2.imencode('.jpg', frame)[1]
    serv.send_response(200)
    serv.send_header('Content-Length', len(img_data_ret))
    serv.send_header('Content-Type', 'image/jpeg')
    serv.end_headers()
    serv.wfile.write(img_data_ret)

def process_2(serv : BaseHTTPRequestHandler):
    content_length = int(serv.headers['Content-Length'])
    post_data = serv.rfile.read(content_length)
    frame = cv2.imdecode(np.fromstring(post_data, np.uint8), cv2.IMREAD_COLOR)

    face_locations = []
    face_encodings = []
    face_names = []
    small_frame = cv2.resize(frame, (0, 0), fx=0.25, fy=0.25)

    rgb_small_frame = small_frame[:, :, ::-1]
    
    face_locations = face_recognition.face_locations(small_frame)
    face_encodings = face_recognition.face_encodings(small_frame, face_locations)

    face_names = []
    for face_encoding in face_encodings:
        name = "Unknown"
        if len(known_face_encodings) > 0:
            matches = face_recognition.compare_faces(known_face_encodings, face_encoding)
            face_distances = face_recognition.face_distance(known_face_encodings, face_encoding)
            best_match_index = np.argmin(face_distances)
            if matches[best_match_index]:
                name = known_face_names[best_match_index]

        face_names.append(name)
    
    ans = []

    for (top, right, bottom, left), name in zip(face_locations, face_names):
        top *= 4
        right *= 4
        bottom *= 4
        left *= 4
        ans.append({'x':left, 'y':top, 'w':right-left, 'h':bottom-top, 'name':name})
    
    img_data_ret = cv2.imencode('.jpg', frame)[1]
    serv.send_response(200)
    serv.send_header('Content-Type', 'text/json')
    serv.end_headers()
    serv.wfile.write(JSONEncoder().encode(ans).encode('utf-8'))

def getFaceEncoding(serv : BaseHTTPRequestHandler):
    content_length = int(serv.headers['Content-Length'])
    post_data = serv.rfile.read(content_length)
    frame = cv2.imdecode(np.fromstring(post_data, np.uint8), cv2.IMREAD_COLOR)

    cv2.imwrite('./amogus.jpg', frame)

    face_encodings = []
    small_frame = cv2.resize(frame, (0, 0), fx=1, fy=1)
    
    face_encodings = face_recognition.face_encodings(small_frame)

    if len(face_encodings) != 1:
        serv.send_response(400)
        serv.send_header('Content-Type', 'text/plain')
        serv.end_headers()
        serv.wfile.write((str(len(face_encodings)) + ' faces found (not 1)').encode('utf-8'))
        return

    serv.send_response(200)
    serv.send_header('Content-Type', 'text/plain')
    serv.end_headers()
    arr = struct.pack('d' * 128, *(face_encodings[0]))
    serv.wfile.write(arr)