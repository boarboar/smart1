import network
import time
import ujson
import gc
import socket,select
from machine import Pin

def handle_http(client, client_addr):
    led.value(1)
    systime = int(round(time.time() * 1000)) 
    sensors = [
        {'I': 1, 'T': 205, 'V': 3010, 'H' : 900, 'HD' : 2, 'X' : systime},
        {'I': 2, 'T': 215, 'V': 2910, 'H' : 0, 'HD' : 0, 'X' : systime},
        ]

    client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n %s" % ujson.dumps(sensors))
    client.close()
    led.value(0)
    gc.collect()
	
def serv(port=80, udpport=9998):
    http = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
    addr = (socket.getaddrinfo("0.0.0.0", port))[0][-1]
    http.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    http.bind(addr)
    http.listen(4)
    print("Web server started on %s" % str(addr))
    udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
    udpaddr = (socket.getaddrinfo("0.0.0.0", udpport))[0][-1]
    udp.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
    udp.bind(udpaddr)
    print("UDP server started on %s" % str(udpaddr))
	
    while True:
        r, w, err = select.select((http,), (), (), 1)
        if r:
            for readable in r:
                client, client_addr = http.accept()
                handle_http(client, client_addr)

print('Main module started')
print('Connecting to network...')

led = Pin(2, Pin.OUT)
led.value(1)

sta_if = network.WLAN(network.STA_IF)
sta_if.active(True)
sta_if.connect('NETGEAR', 'boarboar')

while sta_if.isconnected() == False:
    time.sleep(1)

led.value(0)
	
print('Connection successful')
print(sta_if.ifconfig())

gc.collect()

serv()




  


