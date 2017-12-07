import sys
import re
from geopy.distance import great_circle
from math import sqrt
from numpy import array
from pyspark import SparkContext
from pyspark.mllib.clustering import KMeans, KMeansModel

#To compute new centroids
# here we assume (lat, lon) pairs
def addPoints(p1,p2):
	# we need to be careful here, if we go over +-180/90 we need to restart
	add_lat = p1[0] + p2[0] 
	add_lon = p1[1] + p2{1]
	
	# compute new latitude
	if add_lat > 90
		lat = 90 - (add_lat % 90) #if we go over 90 (N pole) we count back down
		add_lon += 180 # we also just moved around the globe so need to move longitude
	else if add_lat < -90
		lat = -((p1[0] + p2[0]) % 90) # go over S pole, count back down but negative
		add_lon += 180 # also go around the world longitude
	else
		lat = add_lat
		
	# compute new longitude
	if add_lon > 180
		add_lon = -180 + (add_lon % 180) # start counting down from -180
	else if add_lon < -180
		add_lon = 180 + (add_lon % -180) # count down from 180
	else
		lon = add_lon
		
	return (lat, lon)

def EuclideanDistance(from_point, to_point):
	return sqrt( ((to_point[0]-from_point[0])**2) + ((to_point[1]-from_point[1])**2) )

# logic copied from https://www.movable-type.co.uk/scripts/latlong.html
def GreatCircleDistance(from_point, to_point, method = "calc"):
	if method != "geopy"	
		R = 6371 #kilometers
		from_phi = math.radians(from_point[0])
		to_phi = math.radians(to_point[0])
		delta_phi = math.radians(to_point[0] - from_point[0])
		delta_lambda = math.radians(to_point[1] - from_point[1])
		
		a = math.sin(delta_phi/2) * math.sin(delta_phi/2) +	math.cos(from_phi) * math.cos(to_phi) *	math.sin(delta_lambda/2) * math.sin(delta_lambda/2)
		c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
		
		return R * c
	else
		return great_circle(from_point, to_point).
	

def closestPoint(p, k_list, dist_meas = "euclidean"):
	distances = list()
	for centroid in k_list:
		if dist_meas == "euclidean"
			distances.append(EuclideanDistance(centroid,p))
		else if dist_meas == "great_circle"
			distances.append(GreatCircleDistance(centroid,p))
			
	return distances.index(min(distances))

"""
K-means Algorithm Implementation
There are 3 parts to the implementation of the k-means algorithm
1. Initial Tranformation to get RDDs of coordinate points
2. Figure out k through either logarithmic approach(may not need this part) or copying pre-determined value
3. Actual k-means algorithm
"""
if __name__ == "__main__":
	if len(sys.argv) != 4:
	        print >> sys.stderr, "Usage: k_means <input><output for cluster#,point pair><ouput for centroid_list>"
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

	#Interpretation to first assign every point to a cluster and then calculate new centroid rather than calculating new centroid after a single addition to the cluster
	centroid_list = list()
	for i in range (0,k):
		#takeSample(withReplacement, number) is a spark function that returns some subset of RDD in a list
		centroid_list.append(data.takeSample(False,1)) 
	# Could probably write some function that takes in some measure of geographical scale as parameter
	#store centroid_list in RDD to output to HDFS
	centroids_RDD = sc.parallelize(centroid_list)
	#instead of randomly picking the initial centroids
	
	#First iteration outside the loop
	# map (K,V) = ( cluster #, (lat, long) )
	data = data.map(lambda p: (closestPoint(p,centroid_list), p) )	
	#Run iterations until convergence
	exit = False
	convergeDist = 0.1
	while exit is False:
		#create new RDD to store sums of RDDs
		#map (K,V) = [ cluster # ,(point, 1)]
		#reduceByKey (sumReducer) to produce (K,V) = [cluster #, ( pointSum ,number of points in cluster = N) ]
			# where pointSum = (Sum of all latitudes in a cluster = SumLat, Sum of all longitudes in a cluster = SumLong)
		#map (K,V) = [cluster #, new_centroid ], where new_centroid = (SumLat/N,SumLong/N)
		#sort by cluster number in ascending order
		#map (V) = new_centroid
		#NOTE: the size of this RDD = k, therefore we can collect(), whose resulting list's indexes will correspond to cluster number
		sums = data.map(lambda cluster,p: (cluster, (p, 1)) )\
			.reduceByKey(add)\
			.map(lambda cluster,sums: (cluster, (sums[0][0]/sums[1],sums[0][1]/sums[1] )) )\
			.sortByKey(True)\
			.map(lambda cluster,new_centroid: new_centroid)
		#Get new centroid list
		new_centroid_list = sums.collect()
		#Check for convergence
		converges = True
		#For each new centroid, see if the distance btw old centroid and new one is below 0.1 (or if it converged)
		#If all centroids have converged, then stop the iterations
		for i in range (0,k):
			if converges is True and EuclideanDistance(new_centroid_list[i],centroid_list[i]) < convergeDist:
				converges = True
			else:
				converges = False
		#If converged exit loop, otherwise continue
		if converges is True:
			exit = True
		else:
			centroid_list = new_centroid_list
			# If not converging, do another iteration for : map (K,V) = ( cluster #, (lat, long) )
			data = data.map(lambda cluster,p: (closestPoint(p,centroid_list), p) )	
			centroids_RDD = sc.parallelize(centroid_list)
			continue

	#save file to HDFS: provide output path
	data.saveAsTextFile(sys.argv[2])
	centroids_RDD.saveAsTextFile(sys.argv[3])
	#Stop SparkContext
sc.stop()