import network
import time
import json
import gc
import socket,select
from machine import Pin

def handle_http(client, client_addr):    
    led.value(1)
    #client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n %s" % json.dumps(sensors))
    client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n ")
    client.send(json.dumps(list(sensor_data.values())))
    client.close()
    led.value(0)
    gc.collect()

def handle_udp(udp):
    led.value(1)
    data,addr = udp.recvfrom(256)
    sdata = data.decode('utf-8')
    print("Recv UDP: %s" % sdata)
    
    try :
        sobj = json.loads(sdata)
        sobj["X"] = int(round(time.time() * 1000))
        #print("As meas" % str(m))
        #print("Load ok for sensor %s" % str(sobj['I']))
        id = int(sobj['I'])
        if id in sensor_data :
            print("Replace data for %s" % str(id))
            sensor_data[id] = sobj
        else :
            print("New data for %s" % str(id))
            sensor_data[id] = sobj        
    except ValueError:
        print('Invalid value!')    
    except Exception as e:
        print('Something wrong: %s' % str(e)) 
        
    print(json.dumps(sensor_data))
        
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
    input = [http,udp]
    while True:
        r, w, err = select.select(input, (), (), 1)
        if r:
            for readable in r:
                if readable==http:			    
                    client, client_addr = http.accept()
                    handle_http(client, client_addr)
                elif readable==udp:	
                    handle_udp(udp)                    
                else:
                    continue    

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

sensor_data = {}

serv()




  


