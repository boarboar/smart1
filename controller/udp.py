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
    obj = {'I': 2, 'T': 285, 'V': 3610, 'H' : 977, 'DH' : 1}
    sd = json.dumps(obj)
    s.sendto(sd, ('192.168.1.146',9998))
    s.close()
    print("UDP sent %s" % sd)
    

if __name__ == '__main__':
    #send_tcp()
    send_udp()