from http.server import BaseHTTPRequestHandler
from http.server import HTTPServer
from image_proc import *

def run(server_class=HTTPServer, handler_class=BaseHTTPRequestHandler):
    server_address = ('', 8000)
    httpd = server_class(server_address, handler_class)
    try:
        httpd.serve_forever()
    except KeyboardInterrupt:
        httpd.server_close()

class HttpHandler(BaseHTTPRequestHandler):
    def do_GET(self):
        if self.path == '/reload_db':
            reloadImages()
            self.send_response(200)
            self.send_header('Content-Type', 'text/plain')
            self.end_headers()
            self.wfile.write('OK'.encode('utf-8'))
            return
        if self.path == '/index.html':
            with open('./index.html', 'r') as f:
                data = '\n'.join(f.readlines()).encode('utf-8')
                self.send_response(200)
                self.send_header('Content-Type', 'text/html')
                self.end_headers()
                self.wfile.write(data)
                return
        self.send_response(404)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write('Server has cringed of your shitty request'.encode('utf-8'))

    def do_POST(self):
        if self.path == '/process_1':
            process_1(self)
            return
        if self.path == '/process_2':
            process_2(self)
            return
        self.send_response(404)
        self.send_header('Content-Type', 'text/plain')
        self.end_headers()
        self.wfile.write('Server has cringed of your shitty request'.encode('utf-8'))

run(handler_class=HttpHandler)