import network
import time
import json
import gc
import socket,select
import machine
from machine import Pin

def handle_msg(sdata):
    global mc
    try :
        sobj = json.loads(sdata)
        sobj["X"] = int(round(time.time() * 1000))
        #print("As meas" % str(m))
        #print("Load ok for sensor %s" % str(sobj['I']))
        id = int(sobj['I'])
        t = int(sobj['T'])
        if t < 1000 and t > -1000 :
            if id in sensor_data :
                print("Replace data for %s" % str(id))
                sensor_data[id] = sobj
            else :
                print("New data for %s" % str(id))
                sensor_data[id] = sobj    
        else :
            print("Invalid data for %s" % str(id))
            
        mc = mc + 1    
        #print(json.dumps(sensor_data))
        print("avg duration between msgs: %s s" % str(int( round((time.time()-start_time)/mc) )))
    
    except ValueError:
        print('Invalid value!')    
    except Exception as e:
        print('Msg handler error: %s' % str(e)) 

def handle_http(client, client_addr):    
    led.value(1)
    try :
        #client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n %s" % json.dumps(sensors))
        client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\n\r\n ")
        client.send(json.dumps(list(sensor_data.values())))
        client.close()
    except Exception as e:
        print('HTTP handler error: %s' % str(e)) 
    led.value(0)
    gc.collect()
    
def handle_tcp(client, client_addr):    
    led.value(1)
    try :
        data = client.recv(1024)
        if data:
            #sdata = data.decode('utf-8')
            print("Recv TCP: %s" % data)
            handle_msg(data)
    except Exception as e:
        print('TCP handler error: %s' % str(e)) 
    led.value(0)
    gc.collect()

def handle_udp(udp):
    led.value(1)
    try :
        data,addr = udp.recvfrom(256)
        sdata = data.decode('utf-8')
        print("Recv UDP: %s" % sdata)
        handle_msg(sdata) 
    except Exception as e:
        print('UDP handler error: %s' % str(e))         
    led.value(0)
    gc.collect()
	
def serv(port=80, tcpport=9999, udpport=9998):
    try :
        #start http 
        http = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        addr = (socket.getaddrinfo("0.0.0.0", port))[0][-1]
        http.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        http.bind(addr)
        http.listen(4)
        print("Web server started on %s" % str(addr))
        #start tcp
        tcp = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
        tcpaddr = (socket.getaddrinfo("0.0.0.0", tcpport))[0][-1]
        tcp.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        tcp.bind(tcpaddr)
        tcp.listen(4)
        print("TCP server started on %s" % str(tcpaddr))
        #start udp
        udp = socket.socket(socket.AF_INET, socket.SOCK_DGRAM)
        udpaddr = (socket.getaddrinfo("0.0.0.0", udpport))[0][-1]
        udp.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        udp.bind(udpaddr)
        print("UDP server started on %s" % str(udpaddr))
        input = [http, tcp, udp]
        while True:
            r, w, err = select.select(input, (), (), 1)
            if r:
                for readable in r:
                    if readable==http:			    
                        client, client_addr = http.accept()
                        handle_http(client, client_addr)
                    elif readable==udp:	
                        handle_udp(udp) 
                    elif readable==tcp:	
                        client, client_addr = tcp.accept()
                        handle_tcp(client, client_addr)                      
                    else:
                        continue    
    except Exception as e:
        print('Service error: %s' % str(e))
        print('Restsrting...')
        machine.reset()
        
        
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

start_time = time.time()
mc = 0   
        

serv()




  


