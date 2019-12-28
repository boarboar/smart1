from socket import *
import json

def send_tcp():
    s = socket(AF_INET,SOCK_STREAM)
    s.connect(('localhost',8888))
    data="TCP "*4
    s.send(data)
    s.close()

def send_udp():
    s = socket(AF_INET,SOCK_DGRAM)
    obj = {'I': 1, 'T': 205, 'V': 3010, 'H' : 900, 'HD' : 2}
    sd = json.dumps(obj)
    s.sendto(sd, ('192.168.1.144',9998))
    s.close()
    print("UDP sent %s" % sd)
    

if __name__ == '__main__':
    #send_tcp()
    send_udp()