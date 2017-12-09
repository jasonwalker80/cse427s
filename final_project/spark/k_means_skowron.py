import sys
import re
import math
from pyspark import SparkContext

#To compute new centroids
def getCartesianCoordiantes(coords):
    # convert to radians
    (rad_lat, rad_lon) = (math.radians(coords[0]), math.radians(coords[1]))

    # calculate cartesian coordinates
    (x, y, z) = (math.cos(rad_lat) * math.cos(rad_lon), math.cos(rad_lat) * math.sin(rad_lon), math.sin(rad_lat))

    return (x, y, z)

# assumes in cartesian coords
def addPoints(p1,p2):
    return (p1[0] + p2[0], p1[1] + p2[1], p1[2] + p2[2])

def getSphericalCoordinate(x,y,z):
    # convert to spherical
    lon = math.atan2(y, x)
    r = math.sqrt(x**2 + y**2)
    lat = math.atan2(z, r)

    # convert to degrees
    return (math.degrees(lat), math.degrees(lon))

def EuclideanDistance(from_point, to_point):
    return math.sqrt( ((to_point[0]-from_point[0])**2) + ((to_point[1]-from_point[1])**2) )

# logic copied from https://www.movable-type.co.uk/scripts/latlong.html
def GreatCircleDistance(from_point, to_point):
    if method != "geopy":
	    R = 6371 #kilometers
	    from_phi = math.radians(from_point[0])
	    to_phi = math.radians(to_point[0])
	    delta_phi = math.radians(to_point[0] - from_point[0])
	    delta_lambda = math.radians(to_point[1] - from_point[1])
	
	    a = math.sin(delta_phi/2) * math.sin(delta_phi/2) +	math.cos(from_phi) * math.cos(to_phi) *	math.sin(delta_lambda/2) * math.sin(delta_lambda/2)
	    c = 2 * math.atan2(math.sqrt(a), math.sqrt(1-a))
	
	    return R * c
    else:
	    return 0


def CalculateDistance(from_point, to_point, measure):
    if measure == "euclidean":
	    return EuclideanDistance(from_point, to_point)
    elif measure == "great_circle":
	    return GreatCircleDistance(from_point, to_point)
    else:
	    return 0
		
def closestPoint(p, k_list, dist_meas):
    distances = list()
    for centroid in k_list:
	    distances.append(CalculateDistance(centroid,p,dist_meas))
    return distances.index(min(distances))

"""
K-means Algorithm Implementation
There are 3 parts to the implementation
1. Initial Tranformation to get RDDs of coordinate points
2. Figure out k through either logarithmic approach(may not need this part) or copying pre-determined value
3. Actual k-means algorithm
"""
if __name__ == "__main__":
    if len(sys.argv) != 6:
      print >> sys.stderr, "Usage: k_means <input_file> <distmeas> <k> <output for cluster#,point pair> <ouput for centroid_list>"
      exit (-1)

    #create SparkContext object
    sc = SparkContext()	

    #PART 1
	#Transform into RDD containing location coordinates (latitude, longitude) in an array
    data = sc.textFile(sys.argv[1])\
      .map(lambda line: line.split("\t"))\
	  .filter(lambda lcount: len(lcount) == 3)\
	  .map(lambda line: (float(line[0]), float(line[1])))\
      .persist()

    # PART 2
    #Figure out value of k (Implement if needed)
    dist_meas = sys.argv[2]
    k = int(sys.argv[3])

	#PART 3
	#Actual k-means Algorithm Implementation for geo-locations
	#Asumptions:
	#    The RDD has been transformed to a pair RDD containing N pairs of locational coordinates
	#    Geo-locations are probably 2D (longitude, latitutde)

	#CODE:
	# 1. Pick initial k points (store in a list)
	# 2. For remaining p not in centroid_list, add to cluster with closest centroid

	# take a random sample of k values to be the initial centroids
	# there are other ways to do this as well
    centroid_list = list()
    centroid_list = data.takeSample(False, k)

	#store centroid_list in RDD to output to HDFS
    centroids_RDD = sc.parallelize(centroid_list)

	#First iteration outside the loop
	# map (K,V) = ( cluster #, (lat, long) )
    clusterMap = data.map(lambda p: (closestPoint(p,centroid_list,dist_meas), p) )\
	  .persist()

	#Run iterations until convergence
    stop = False
    convergeDist = 0.1
    while stop is False:
        cent_coords = clusterMap.map(lambda pnt: (pnt[0], getCartesianCoordiantes(pnt[1])))\
            .reduceByKey(lambda pnt1, pnt2: addPoints(pnt1, pnt2))\
            .map(lambda v: (v[0], getSphericalCoordinate(v[1][0], v[1][1], v[1][2])))\
            .sortByKey(True)\
		    .persist()
			
	    #Get new centroid list
        new_centroid_list = cent_coords.map(lambda coord: coord[1]).collect()

        #Check for convergence
        converges = True
		#For each new centroid, see if the distance btw old centroid and new one is below 0.1 (or if it converged)
		#If all centroids have converged, then stop the iterations
        for i in range (0,k):
	        if converges is True and CalculateDistance(new_centroid_list[i],centroid_list[i], dist_meas) < convergeDist:
		        converges = True
	        else:
		        converges = False
        #If converged, exit loop, otherwise continue
        if converges is True:
	        stop = True
        else:
            centroid_list = new_centroid_list
	        # If not converging, do another iteration for : map (K,V) = ( cluster #, (lat, long) )
            clusterMap = data.map(lambda p: (closestPoint(p,centroid_list,dist_meas), p) )\
                .persist()
            centroids_RDD = sc.parallelize(centroid_list)
            centroids_RDD.persist()

    clusterMap.saveAsTextFile(sys.argv[4])
    centroids_RDD.saveAsTextFile(sys.argv[5])

	#Stop SparkContext
    sc.stop()
