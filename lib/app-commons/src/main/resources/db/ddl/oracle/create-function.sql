CREATE FUNCTION DBSINGULAR.dateDiffInDays (DATE1 in TIMESTAMP, DATE2 in TIMESTAMP)
RETURN float
BEGIN
   RETURN(DATE1 - DATE2)
END
