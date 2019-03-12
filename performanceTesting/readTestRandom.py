#!/usr/bin/python3

import random, string, subprocess, time

requests = 10000

start = time.time()

for i in range(requests):
	shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(20))
	request="http://localhost:2020/"+shortResource	# print(request)
	subprocess.call(["curl", "-X", "GET", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

end = time.time()

print("\nreadTestRandom:\n")
print("# of Requests: ",requests)
print("Total Time: ",(end - start),"s")
print("Time per Request: ",(end - start)/requests,"s\n")
