import sys
from pyspark import SparkContext

if __name__ == "__main__":
	if len(sys.argv) != 2:
		print >> sys.stderr, "Usage: countjpgs <file>"
		exit(-1)
	
	# get spark context
	sc = SparkContext()

	# read file into spark and count jpgs
	jpgCount = sc.textFile(sys.argv[1]) \
		.map(lambda line: line.split(" ")) \
		.filter(lambda fields: "jpg" in fields[6]) \
		.count()

	# print results
	print jpgCount

	# stop spark
	sc.stop()
