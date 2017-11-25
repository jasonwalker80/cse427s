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
      print >> sys.stderr, "Usage: WordCount <file>"
      exit (-1)

    #create SparkContext object
    sc = SparkContext()

    #define all regexex first
    valid_regex = re.compile(r"\d{4}-\d{2}-\d{2}:\d{2}:\d{2}:\d{2}[,|][\w\s.]+[,|]\w{8}-\w{4}-\w{4}-\w{4}-\w{12}[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([\d]+|enabled|disabled)[,|]([-]{0,1}[\d]{1,3}[.][\d]+|0)[,|]([-]{0,1}[\d]{1,3}[.][\d]+|0)")
    location_regex = re.compile(r"([-]{0,1}[\d]{1,3}[.][\d]+|0)")
    date_regex = re.compile(r"\d{4}-\d{2}-\d{2}:\d{2}:\d{2}:\d{2}")
    model_regex = re.compile(r"[\w\s.]+")
    deviceID_regex = re.compile(r"\w{8}-\w{4}-\w{4}-\w{4}-\w{12}")

    #Load file
    # Filter out invalid records
    # Map into the following list (longitude, latitude, date, model, device ID)
    # Map the same thing but with float values for longiude and latitude and do some additional scrubbing
    # Filter out the records where (longitude, latitude) = (0,0)
    # Map the same but with Model Name and Manufacturer name split so that the RDD in the following format: (longitude, latitude, date, manufacturer, model, device ID )
    data = sc.textFile("file:/home/training/training_materials/dev1/data/devicestatus.txt")\
      .filter(lambda line: valid_regex.match(line))\
      .map(lambda line: (str((location_regex.findall(line))[-2]), str((location_regex.findall(line))[-1]), str(date_regex.findall(line)), str((model_regex.findall(line))[6]), str(deviceID_regex.findall(line)) ) )\
      .map(lambda x: (float(x[0]), float(x[1]), x[2].replace("[u'","").replace("']",""), x[3], x[4].replace("[u'","").replace("']","") ) )\
      .filter(lambda x: ((x[0]==0)!=(x[1]==0)) or ((x[0]!=0) and (x[1]!=0)) )\
      .map(lambda x: (x[0], x[1], x[2], x[3].split(" ")[0], x[3].split(" ")[1], x[4]) )
    data = data.map(lambda line: toCSVLine(line))

    #Save file
    data.saveAsTextFile("hdfs:/loudacre/devicestatus_etl")
    #Stop the SparkContext object
    sc.stop()
