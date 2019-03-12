#!/usr/bin/python3

import random, subprocess, string, time

requests = 10000

start = time.time()

for i in range(requests):
	request="http://localhost:2020/000000000000000000000000000000000000000000000"
	# print(request)
	subprocess.call(["curl", "-X", "GET", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

end = time.time()

print("\nreadTest:\n")
print("# of Requests: ",requests)
print("Total Time: ",(end - start),"s")
print("Time per Request: ",(end - start)/requests,"s\n")
