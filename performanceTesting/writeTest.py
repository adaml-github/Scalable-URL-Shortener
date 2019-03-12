#!/usr/bin/python3

import random, string, subprocess, time

requests = 10000

start = time.time()

for i in range(requests):
	longResource = "http://"+''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(100))
	shortResource = ''.join(random.choice(string.ascii_uppercase + string.digits) for _ in range(20))

	request="http://localhost:2020/?short="+shortResource+"&long="+longResource
	# print(request)
	subprocess.call(["curl", "-X", "PUT", request], stdout=subprocess.DEVNULL, stderr=subprocess.DEVNULL)

end = time.time()

print("\nwriteTest:\n")
print("# of Requests: ",requests)
print("Total Time: ",(end - start),"s")
print("Time per Request: ",(end - start)/requests,"s\n")
