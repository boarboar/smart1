#!/usr/bin/env python
# Reflects the requests with dummy responses from HTTP methods GET, POST, PUT, and DELETE
# Written by Tushar Dwivedi (2017)

import json
import time
from BaseHTTPServer import HTTPServer, BaseHTTPRequestHandler
from optparse import OptionParser

class RequestHandler(BaseHTTPRequestHandler):

    def do_GET(self):
        request_path = self.path

	

        print("\n----- Request Start ----->\n")
        print("request_path :", request_path)
        print("self.headers :", self.headers)
        print("<----- Request End -----\n")
        
        #time.sleep(60.0)

        systime = int(round(time.time() * 1000))
        
        sensors = [
        {'I': 1, 'T': 205, 'V': 3010, 'H' : 900, 'HD' : 2, 'X' : systime},
        {'I': 2, 'T': 215, 'V': 2910, 'H' : 0, 'HD' : 0, 'X' : systime},
	{'I': 5, 'T': -215, 'V': 2910, 'H' : 0, 'HD' : 0, 'X' : systime},
        ]

        self.send_response(200)
        #self.send_header("Set-Cookie", "foo=bar")
        #self.send_header('Content-Type', 'application/json')
        self.end_headers()
        self.wfile.write(json.dumps(sensors))

    def do_POST(self):
        request_path = self.path

        # print("\n----- Request Start ----->\n")
        print("request_path : %s", request_path)

        request_headers = self.headers
        content_length = request_headers.getheaders('content-length')
        length = int(content_length[0]) if content_length else 0

        # print("length :", length)

        print("request_headers : %s" % request_headers)
        print("content : %s" % self.rfile.read(length))
        # print("<----- Request End -----\n")

        self.send_response(200)
        self.send_header("Set-Cookie", "foo=bar")
        self.end_headers()
        self.wfile.write(json.dumps({'hello': 'world', 'received': 'ok'}))

    do_PUT = do_POST
    do_DELETE = do_GET


def main():
    port = 8080
    print('Listening on localhost:%s' % port)
    server = HTTPServer(('', port), RequestHandler)
    server.serve_forever()


if __name__ == "__main__":
    parser = OptionParser()
    parser.usage = ("Creates an http-server that will echo out any GET or POST parameters, and respond with dummy data\n"
                    "Run:\n\n")
    (options, args) = parser.parse_args()

    main()