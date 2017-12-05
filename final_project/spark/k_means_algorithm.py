import sys
import re
from geopy.distance import great_circle
from math import sqrt
from numpy import array
from pyspark import SparkContext
from pyspark.mllib.clustering import KMeans, KMeansModel

#To compute new centroids
def addPoints(p1,p2):
	return (p1[0]+p2[0], p1[1]+p2[1])

def EuclideanDistance(from_point, to_point):
	return sqrt( ((to_point[0]-from_point[0])**2) + ((to_point[1]-from_point[1])**2) )

def GreatCircleDistance(from_point, to_point):
	return great_circle(from_point, to_point).miles

def closestPoint(p, k_list):
	distances = list()
	for centroid in k_list:
		distances.append(EuclideanDistance(centroid,p))
	return distances.index(min(distances))

"""
K-means Algorithm Implementation

There are 3 parts to the implementation of the k-means algorithm
1. Initial Tranformation to get RDDs of coordinate points
2. Figure out k through either logarithmic approach(may not need this part) or copying pre-determined value
3. Actual k-means algorithm
"""
if __name__ == "__main__":
	if len(sys.argv) != 3:
	        print >> sys.stderr, "Usage: k_means <input><output>"
	        exit (-1)

	#create SparkContext object
	sc = SparkContext()

	#PART 1
	#Transform into RDD containing location coordinates (latitude, longitude) in an array
	data = sc.textfile(sys.argv[1])\
		#The operations to transform into pair RDD depend on the format of that data
		.map(lambda line: array([float(x) for x in line.split(',') ]) )\
		.distinct()\
		.persist()

	#PART 2
	#Figure out value of k (Implement if needed)
	

	#PART 3
	#Actual k-means Algorithm Implementation for geo-locations
	#Asumptions:
	#    The RDD has been transformed to a pair RDD containing N pairs of locational coordinates
	#    Geo-locations are probably 2D (longitude, latitutde)
	
	#CODE:
	# 1. Pick initial k points (store in a list)
	k = 5 #Pick k either determined by PART 2 or pre-determined
	# 2. For remaining p not in centroid_list, add to cluster with closest centroid
	# USING a Package:
	clusters = KMeans.train(data, k, initializationMode="random")
	clusters.save(sc, sys.argv[2])
	#ATTEMP to make my own version w/out using a package (either version 1 or version 2 is a misinterpretted version, not sure which at this point)
	#VERSION 1
	centroid_list = list()
	sums = list()
	Ns = list()
	for i in range (0,k):
		centroid_list.append() #How to exactly append points likely to be in different clusters?
		sums.append()
		Ns.append(1)
	# Could probably write some function that takes in some measure of geographical scale as parameter
	# QUESTION: How to change centroid_list after 1 point has been assigned a cluster
	#	map (K,V) = (cluster #, (lat,long) ) and update centroid_list
	# Does this even work? anyways, the idea is to do the operations immediately after adding a point to a cluster , and before moving on to the next point
	data = data.map(lambda p: index=closestPoint(p,centroid_list), sums[index]=addPoints(p,sums[index]), Ns[index]=Ns[index]+1, centroid_list[index]=sums[index]/Ns[index], (index ,p))\
		.persist()

	#VERSION 2: Interpretation to first assign everything to a cluster and then calculate new centroid rather than calculating new centroid after a single addition to the cluster
	centroid_list = list()
	N = data.count()
	for i in range (0,k):
		centroid_list.append() #How to exactly append points likely to be in different clusters?
	# Could probably write some function that takes in some measure of geographical scale as parameter
	
	#First iteration outside the loop
	# map (K,V) = ( cluster #, (lat, long) )
	data = data.map(lambda p: (closestPoint(p,centroid_list), p) )	
	#Run iterations until convergence
	exit = False
	convergeDist = 0.1
	while exit is False:
		#create new RDD to store sums of RDDs
		sums = data.map(lambda line: (line, 1) )\
			.reduceByKey(add)\
			.map(lambda cluster,sums,N: (cluster, (sums[0]/N,sums[1]/N)) )\
			.sortByKey(True)\
			.map(lambda cluster,new_centroid: new_centroid)
		#Check for convergence
		new_centroid_list = sums.collect()
		converges = True
		for i in range (0,k):
			if converges is True and EuclideanDistance(new_centroid_list[i],centroid_list[i]) < convergeDist:
				converges = True
			else:
				converges = False

		if converges is True:
			exit = True
		else:
			centroid_list = new_centroid_list
		# If not converging, do another iteration for : map (K,V) = ( cluster #, (lat, long) )
		data = data.map(lambda cluster,p: (closestPoint(p,centroid_list), p) )	

	#save file to HDFS: provide output path
	data.saveAsTextFile(sys.argv[2])
	#Stop SparkContext
	sc.stop()
