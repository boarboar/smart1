from socket import *
import json

def send_tcp():
    s = socket(AF_INET,SOCK_STREAM)
    s.connect(('192.168.1.144',9999))
    obj = {'I': 1, 'T': -1020, 'V': 4610, 'H' : 444, 'DH' : 1}
    sd = json.dumps(obj)
    s.send(sd)
    s.close()
    print("TCP sent %s" % sd)

def send_udp():
    s = socket(AF_INET,SOCK_DGRAM)
    obj = {'I': 2, 'T': 235, 'V': 3610, 'H' : 777, 'DH' : 2}
    sd = json.dumps(obj)
    s.sendto(sd, ('192.168.1.144',9998))
    s.close()
    print("UDP sent %s" % sd)
    

if __name__ == '__main__':
    send_tcp()
    #send_udp()