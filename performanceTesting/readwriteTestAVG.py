#!/usr/bin/python3

import random, string, subprocess, time

HOST = "2020"

requests = 10000

start = time.time()

for i in range(625): #625 because we wont change the # of requests, so cache is size 16, and 10k/16 is 625
	
	#read random
	for j in range(4):
		shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(20))
		request="http://localhost:"+HOST+"/"+shortResource	# print(request)
		subprocess.call(["curl", "-X", "GET", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

	#write random
	for k in range(4):
		longResource = "http://"+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(100))
		shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(20))

		request="http://localhost:"+HOST+"/?short="+shortResource+"&long="+longResource
		# print(request)
		subprocess.call(["curl", "-X", "PUT", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

	#write same
	for l in range(4):
		longResource = "http://"+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(100))
		shortResource = "test"+str(l)

		request="http://localhost:"+HOST+"/?short="+shortResource+"&long="+longResource
		# print(request)
		subprocess.call(["curl", "-X", "PUT", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

	#read same - should be grabbing from cache
	for m in range(4):
		shortResource = "test"+str(m)
		request="http://localhost:"+HOST+"/"+shortResource	# print(request)
		subprocess.call(["curl", "-X", "GET", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)



end = time.time()

print("\nreadwriteTestAVG:\n")
print("# of Requests: ",requests)
print("Total Time: ",(end - start),"s")
print("Time per Request: ",(end - start)/requests,"s\n")
