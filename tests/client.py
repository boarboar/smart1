import socket
  
TCP_IP = '192.168.1.121'
TCP_PORT = 9999
#BUFFER_SIZE = 1024
MESSAGE = '{"I":"2","M":64,"P":0,"R":8,"T":220,"V":320}'
#MESSAGE = '{"II":"2","M":64,"P":0,"R":8,"T":320,"V":320'
#MESSAGE = '{"I":"2","M":64,"P":0,"R":8}'

s = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
s.connect((TCP_IP, TCP_PORT))
s.send(MESSAGE)
#data = s.recv(BUFFER_SIZE)
s.close()
# print "received data:", data