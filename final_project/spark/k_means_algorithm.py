import sys
import re
from geopy.distance import great_circle
from math import sqrt
from pyspark import SparkContext

#To compute new centroids
def addPoints(p1,p2):
	# convert to radians
	(p1_lat, p1_lon) = (math.radians(p1[0]), math.radians(p1[1]))
	(p2_lat, p2_lon) = (math.radians(p2[0]), math.radians(p2[1]))
	
	# calculate cartesian coordinates
	(p1_x, p1_y, p1_z) = (math.cos(p1_lat) * math.cos(p1_lon), math.cos(p1_lat) * math.sin(p1_lon), math.sin(p1_lat))
	(p2_x, p2_y, p2_z) = (math.cos(p2_lat) * math.cos(p2_lon), math.cos(p2_lat) * math.sin(p2_lon), math.sin(p2_lat))
	
	# add points
	(add_x, add_y, add_z) = (p1_x + p2_x, p1_y + p2_y, p1_z + p2_z)

	# convert to spherical
	add_lon = math.atan2(add_y, add_x)
	r = math.sqrt(add_x**2 + add_y**2)
	add_lat = math.atan2(add_z, r)
	
	# convert back to degrees
	return (math.degrees(add_lat), math.degrees(add_lon))

def EuclideanDistance(from_point, to_point):
	return sqrt( ((to_point[0]-from_point[0])**2) + ((to_point[1]-from_point[1])**2) )

def GreatCircleDistance(from_point, to_point):
	return great_circle(from_point, to_point).miles

def closestPoint(p, k_list):
	distances = list()
	for centroid in k_list:
		distances.append(GreatCircleDistance(centroid,p))
	return distances.index(min(distances))

"""
K-means Algorithm Implementation

There are 3 parts to the implementation
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
		.map(lambda line: ())\
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

	#Interpretation to first assign every point to a cluster and then calculate new centroid rather than calculating new centroid after a single addition to the cluster
	centroid_list = list()
	for i in range (0,k):
		#takeSample(withReplacement, number) is a spark function that returns some subset of RDD in a list
		centroid_list.append(data.takeSample(False,1)) 
	# Could probably write some function that takes in some measure of geographical scale as parameter

	#store centroid_list in RDD to output to HDFS
	centroids_RDD = sc.parallelize(centroid_list)
	
	#First iteration outside the loop
	# map (K,V) = ( cluster #, (lat, long) )
	data = data.map(lambda p: (closestPoint(p,centroid_list), p) )\
		.persist()
	#Run iterations until convergence
	stop = False
	convergeDist = 0.1
	while stop is False:
		#create new RDD to store sums of RDDs
		#map (K,V) = [ cluster # ,(point, 1)]
		#reduceByKey (sumReducer) to produce (K,V) = [cluster #, ( pointSum ,number of points in cluster = N) ]
			# where pointSum = (Sum of all latitudes in a cluster = SumLat, Sum of all longitudes in a cluster = SumLong)
		#map (K,V) = [cluster #, new_centroid ], where new_centroid = (SumLat/N,SumLong/N)
		#sort by cluster number in ascending order
		#map (V) = new_centroid
		#NOTE: the size of this RDD = k, therefore we can collect(), whose resulting list's indexes will correspond to cluster number
		sums = data.map(lambda line: (line[0], (line[1], 1)) )\
			.reduceByKey(lambda line1,line2: (line1[0]+line2[0],line1[1]+line2[1],line1[2]+line2[2]) )\
			# or .reduceByKey (lambda line1, line2: (addPoints(line1[0:1], line2[0:1]),line1[2]+line2[2]) )
			.map(lambda line: (line[0], (line[1][0]/line[1][2],line[1][1]/line[1][2] )) )\
			.sortByKey(True)\
			.map(lambda line: line[1])\
			.persist()
		#Get new centroid list
		new_centroid_list = sums.collect()
		#Check for convergence
		converges = True
		#For each new centroid, see if the distance btw old centroid and new one is below 0.1 (or if it converged)
		#If all centroids have converged, then stop the iterations
		for i in range (0,k):
			if converges is True and GreatCircleDistance(new_centroid_list[i],centroid_list[i]) < convergeDist:
				converges = True
			else:
				converges = False
		#If converged, exit loop, otherwise continue
		if converges is True:
			stop = True
		else:
			centroid_list = new_centroid_list
			# If not converging, do another iteration for : map (K,V) = ( cluster #, (lat, long) )
			data = data.map(lambda line: (closestPoint(line[1],centroid_list), line[1]) )\
				.persist()
			centroids_RDD = sc.parallelize(centroid_list)
			centroids_RDD.persist()
			continue
	
	# Transform (cluster #, (lat, long) ) --> (cluster #, lat_of_centroid, long_of_centroid, radius_of_cluster (= max_distance) ,distance_from_centroid_to_point, lat, long)
	#	Step 1: It's difficult to get the masximum distance (=radius) of each cluster. Let's start by creating some new RDD = (cluster, distance_from_centroid_to_point)
	intermerdiate_RDD = data.map(lambda line: (line[0], GreatCircleDistance(line[1],centroid_list[line[0]]) ))
	#	Step 2: Create a new RDD for each cluster by filtering out by cluster number so each new RDD only contains distances for 1 cluster and sort By V = disntaces
	maxDistances = list ()
	for i in range (0,k):
		cluster_RDD = intermerdiate_RDD.filter(lambda line: (line[0]==i)).sortBy(lambda line: line[1], False)
	#	Step 3: Take only the first element of the RDD and store them to a list of maximum distances in order of the cluster number
		maxDistances[i] = cluster_RDD.take(1)[0]

	#	Step 4: data = data.map (cluster #, lat_of_centroid, long_of_centroid, radius_of_cluster (= max_distance), distance_from_centroid_to_point, lat, long)
	data = data.map (lambda line: (line[0], centroid_list[line[0]][0], centroid_list[(line[0]][1], maxDistances[line[0]], GreatCircleDistance(line[1],centroid_list[line[0]]), line[1][0], line[1][1]) )\
		
	#save file to HDFS: provide output path
	data.saveAsTextFile(sys.argv[2])
	centroids_RDD.saveAsTextFile(sys.argv[3])
	#Stop SparkContext
	sc.stop()
