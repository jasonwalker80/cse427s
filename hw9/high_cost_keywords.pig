-- load data
data = LOAD '/dualcore/ad_data[0-9]/part*' AS (campaign_id:chararray,
             date:chararray, time:chararray,
             keyword:chararray, display_site:chararray, 
             placement:chararray, was_clicked:int, cpc:int);

-- group data by keyword
keywordGroup = GROUP data BY keyword;

-- calculate total cost
keywordTotalCost = FOREACH keywordGroup GENERATE group as keyword, SUM(data.cpc) AS total_cost;

-- sort descending
orderedTotalCost = ORDER keywordTotalCost BY total_cost DESC;

-- display top 3 records
top3Records = LIMIT orderedTotalCost 3;
DUMP top3Records;
