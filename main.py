from http.server import BaseHTTPRequestHandler
from http.server import HTTPServer
from random import randint
from image_proc import *
from db_requests import *
import os
import hashlib
import base64
import requests
import ssl

connectToDB()

findUserByEmail("a@b.c")

postTokens = [
    'AMNKJgii34ohtfio3rmnwiuhcJOPHBUIVNUWREFJo34guytif'
]

def run(server_class=HTTPServer, handler_class=BaseHTTPRequestHandler):
    server_address = ('', 8000)
    httpd = server_class(server_address, handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()

class HttpHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if os.path.exists(os.path.join('./', self.path[1:])):
            with open(os.path.join('./', self.path[1:]), 'r') as f:
                data = '\n'.join(f.readlines()).encode('utf-8')
                self.send_response(200)
                self.end_headers()
                self.wfile.write(data)
                return
        if self.path == '/get_posts':
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            posts = getPosts()
            self.wfile.write(JSONEncoder().encode(posts).encode('utf-8'))
            return
        self.send_response(404)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write('Server has cringed of your shitty request\r\n'.encode('utf-8'))

    def do_POST(self):
        print(self.path)
        if self.path == '/register_user':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 4:
                email = base64.b64decode(rcvData[0]).decode('utf-8')
                password = base64.b64decode(rcvData[1]).decode('utf-8')
                fullname = base64.b64decode(rcvData[2]).decode('utf-8')
                faceEncoding = base64.b64decode(rcvData[3])
                print(email, password, fullname)
                if len(findUserByEmail(email)) != 0:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('User already exists\r\n'.encode('utf-8'))
                    return
                uid = registerUser(email, fullname, password, faceEncoding)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(str(uid).encode('utf-8'))
                return
        if self.path == '/login':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 2:
                email = base64.b64decode(rcvData[0]).decode('utf-8')
                password = base64.b64decode(rcvData[1]).decode('utf-8')
                print(email, password)
                uid = getUsersIdByEmailAndPassword(email, password)
                if uid == -1:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Wrong email or password\r\n'.encode('utf-8'))
                    return
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(str(uid).encode('utf-8'))
                return
        if self.path == '/add_delivery':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 5:
                email1 = base64.b64decode(rcvData[0]).decode('utf-8')
                password1 = base64.b64decode(rcvData[1]).decode('utf-8')
                email2 = base64.b64decode(rcvData[2]).decode('utf-8')
                post1 = base64.b64decode(rcvData[3]).decode('utf-8')
                post2 = base64.b64decode(rcvData[4]).decode('utf-8')
                print(email1, password1)
                uid1 = getUsersIdByEmailAndPassword(email1, password1)
                uid2 = getUsersIdByEmail(email2)
                if uid1 == -1:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Wrong email or password\r\n'.encode('utf-8'))
                    return
                if uid2 == -1:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Wrong receiver email\r\n'.encode('utf-8'))
                    return
                if post1 == post2:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Postomaty dolshny byt raznymy\r\n'.encode('utf-8'))
                    return
                if uid1 == uid2:
                    self.send_response(400)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Polzovately dolshny byt raznymy\r\n'.encode('utf-8'))
                    return
                delId = createDelivery(uid1, uid2, post1, post2, randint(1, 10))
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(str(delId).encode('utf-8'))
                return


        if self.path == '/get_delivery_from_sender':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 2:
                postToken = rcvData[0]
                if postToken not in postTokens:
                    self.send_response(403)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Post unauthorized\r\n'.encode('utf-8'))
                    return
                senderId = int(rcvData[1])
                delivId = getOrderFromSender(senderId, postTokens.index(postToken) + 1)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(str(delivId).encode('utf-8'))
                return


        if self.path == '/get_delivery_from_receiver':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 2:
                postToken = rcvData[0]
                if postToken not in postTokens:
                    self.send_response(403)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Post unauthorized\r\n'.encode('utf-8'))
                    return
                receiverId = int(rcvData[1])
                delivId = getOrderFromReceiver(receiverId, postTokens.index(postToken) + 1)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write(str(delivId).encode('utf-8'))
                return


        if self.path == '/delivery_send':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 2:
                postToken = rcvData[0]
                if postToken not in postTokens:
                    self.send_response(403)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Post unauthorized\r\n'.encode('utf-8'))
                    return
                delivId = int(rcvData[1])
                sentDelivery(delivId, postTokens.index(postToken) + 1)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write('OK'.encode('utf-8'))
                return


        if self.path == '/delivery_received':
            content_length = int(self.headers['Content-Length'])
            retdata = self.rfile.read(content_length)
            rcvData = retdata.decode('utf-8').strip().split(';')
            print(rcvData)
            if len(rcvData) == 2:
                postToken = rcvData[0]
                if postToken not in postTokens:
                    self.send_response(403)
                    self.send_header('Content-Type', 'text/plain')
                    self.end_headers()
                    self.wfile.write('Post unauthorized\r\n'.encode('utf-8'))
                    return
                delivId = int(rcvData[1])
                recvdDelivery(delivId, postTokens.index(postToken) + 1)
                self.send_response(200)
                self.send_header('Content-Type', 'text/plain')
                self.end_headers()
                self.wfile.write('OK'.encode('utf-8'))
                return


        if self.path == '/process_1':
            process_1(self)
            return
        if self.path == '/process_2':
            process_2(self)
            return
        if self.path == '/get_face_encoding':
            getFaceEncoding(self)
            return
        self.send_response(404)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write('Server has cringed of your shitty request\r\n'.encode('utf-8'))

run(handler_class=HttpHandler)

closeDB()