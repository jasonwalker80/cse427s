import sys
import re
from pyspark import SparkContext

#NOTE: This file should be run as a Spark application on Local Mode only

#to convert list to CSV line
def toCSVLine(line):
    newLine = "";
    for e in line:
      newLine = newLine + "," + str(e)
    return newLine[1:]

if __name__ == "__main__":
    # check number of command line arguments passed in
    if len(sys.argv) != 1 :
      print >> sys.stderr, "Usage: step1 <file>"
      exit (-1)

    #create SparkContext object
    sc = SparkContext()

    #Load file
    # Filter out invalid records
    # Map into the following list (longitude, latitude, date, model, device ID)
    # Map the same thing but with float values for longiude and latitude and do some additional scrubbing
    # Filter out the records where (longitude, latitude) = (0,0)
    # Map the same but with Model Name and Manufacturer name split so that the RDD in the following format: (longitude, latitude, date, manufacturer, model, device ID )
    data = sc.textFile("file:/home/training/training_materials/dev1/data/devicestatus.txt")\
      .map(lambda line: (line[19],line))\
      .map(lambda lsplit: lsplit[1].split(lsplit[0]))\
      .filter(lambda rcount: len(rcount) == 14)\
      .map(lambda vals: (float(vals[12]), float(vals[13]),vals[0], vals[1], vals[3]))\
      .filter(lambda latlong: (latlong[0] != 0 and latlong[1] != 0))\
      .map(lambda manu: (manu[0], manu[1], manu[2], manu[3].split(" ")[0], manu[3].split(" ")[1], manu[4]))\
      .map(lambda line: toCSVLine(line))

    #Save file
    data.saveAsTextFile("hdfs:/loudacre/devicestatus_etl")
    #Stop the SparkContext object
sc.stop()
