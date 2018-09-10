import SocketServer

class MyTCPHandler(SocketServer.BaseRequestHandler):
    """
    The request handler class for our server.

    It is instantiated once per connection to the server, and must
    override the handle() method to implement communication to the
    client.
    """

    def handle(self):
        # self.request is the TCP socket connected to the client
        self.data = self.request.recv(1024).strip()
        print "{} wrote:".format(self.client_address[0])
        print self.data
        # just send back the same data, but upper-cased
        #self.request.sendall(self.data.upper())
        
#HOST, PORT = "localhost", 9999
HOST, PORT = '', 9999

print("Hi")

# Create the server, binding to localhost on port 9999
server = SocketServer.TCPServer((HOST, PORT), MyTCPHandler)

#ip, port = server.server_address
print("Listening on %s:%s" % server.server_address)

#print 'Socket bind complete to '+str(server.socket.getsockname())

#print("Listening")
# Activate the server; this will keep running until you
# interrupt the program with Ctrl-C
server.serve_forever()