-- TODO (A): Replace 'FIXME' to load the test_ad_data.txt file.
--data = LOAD 'test_ad_data.txt' AS (campaign_id:chararray,
--             date:chararray, time:chararray,
--             keyword:chararray, display_site:chararray, 
--             placement:chararray, was_clicked:int, cpc:int);

-- load with file patterns
data = LOAD '/dualcore/ad_data[0-9]/part*' AS (campaign_id:chararray,
             date:chararray, time:chararray,
             keyword:chararray, display_site:chararray, 
             placement:chararray, was_clicked:int, cpc:int);


-- TODO (B): Include only records where was_clicked has a value of 1
wasClickedOne = FILTER data BY was_clicked == 1;


-- TODO (C): Group the data by the appropriate field
siteGroup = GROUP wasClickedOne BY display_site;


/* TODO (D): Create a new relation which includes only the 
 *           display site and the total cost of all clicks 
 *           on that site
 */
siteTotalCost = FOREACH siteGroup GENERATE group as display_site, SUM(wasClickedOne.cpc) AS total_cost;


-- TODO (E): Sort that new relation by cost (ascending)
orderedTotalCost = ORDER siteTotalCost BY total_cost ASC;

-- TODO (F): Display just the first three records to the screen
top4Records = LIMIT orderedTotalCost 4;
DUMP top4Records;
