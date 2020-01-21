import network
import time
import json
import gc
import socket,select
#import machine
from machine import Pin
from machine import WDT

html_head = """<html><head><title>ESP32 Web Server</title>
<body>
{0} message(s), avg rate {1} s
<br>
<br>
<table border="1">
<tr><th>ID</th><th>Timeout(s)</th></tr>
"""
html_row = "<tr><td>{0}</td><td>{1}</td></tr>"
html_tail = """
</table>
<br>
System uptime: {0}
<br>
Module uptime: {1}
<br>
Free memory: {2}
<br>
Processed in {3} second(s)
</body></html>
"""

def format_time(t):
    t = round(t)
    s = t % 60
    t = t // 60 #minutes
    m = t % 60
    t = t // 60 #hours
    h = t % 24
    d = t // 24 #days
    return "{0} days {1} hours {2} minutes {3} seconds".format(d, h, m, s)

def handle_http(client, client_addr):    
    led.value(1)
    try :
        now = time.time()
        data = client.recv(4096)
        if data:
            sdata = data.decode('utf-8')
            print("Recv HTTP: %s" % sdata)
            if sdata.find('/status') != -1 :
                client.send("HTTP/1.0 200 OK\r\nContent-Type: text/html\r\nConnection: close\r\n\r\n ")
                if mc !=0 :
                    client.send(html_head.format(str(mc), str(int( round((time.time()-start_time)/mc) )) ))
                else :
                    client.send(html_head.format(str(mc), "-"))    
                for s in list(sensor_data.values()) :
                    client.send(html_row.format(str(s['I']), str(int(round((now*1000-s['X'])/1000)))  ))
                client.send(html_tail.format( format_time(now),  format_time(now-start_time), gc.mem_free(), str(int(round(time.time()-now))) ))
            else :    
                client.send("HTTP/1.0 200 OK\r\nContent-Type: application/json\r\nConnection: close\r\n\r\n ")
                #client.send(json.dumps(list(sensor_data.values())))
                client.send(json.dumps(list(filter(lambda s: now*1000-s['X'] < 3600000, sensor_data.values()))))
        client.close()
    except Exception as e:
        print('HTTP handler error: %s' % str(e)) 
    led.value(0)
    gc.collect()
    
def handle_msg(sdata):
    global mc
    try :
        sobj = json.loads(sdata)
        sobj["X"] = int(round(time.time() * 1000))
        #print("As meas" % str(m))
        #print("Load ok for sensor %s" % str(sobj['I']))
        id = int(sobj['I'])
        t = int(sobj['T'])
        if id in sensor_data :
            if t < 1000 and t > -1000 :
                print("Replace data for %s" % str(id))
                sensor_data[id] = sobj
            else :
                print("Invalid data ignored for %s" % str(id))
        else :
            print("New data for %s" % str(id))
            sensor_data[id] = sobj   
            
        mc = mc + 1    
    
    except ValueError:
        print('Invalid value!')    
    except Exception as e:
        print('Msg handler error: %s' % str(e)) 
    
def handle_tcp(client, client_addr):    
    led.value(1)
    try :
        data = client.recv(1024)
        if data:
            sdata = data.decode('utf-8')
            print("Recv TCP: %s" % sdata)
            handle_msg(sdata)
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
            #wdt.feed()
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
        print('Restarting...')
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
        
#wdt = WDT(timeout=60000)  # enable it with a timeout of 1 min

serv()




  


