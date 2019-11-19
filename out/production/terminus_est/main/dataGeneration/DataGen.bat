
FOR %%i IN (20, 50, 100) ^
DO FOR %%j IN (5, 10, 15, 20) ^
DO FOR %%k IN (25, 50, 75) ^
DO FOR /L %%l IN (1, 1, 20) DO ^
Call :AddInstance %%i %%j %%k %%l
  
EXIT /B %ERRORLEVEL%
:AddInstance
echo Nodes: %~1, rSPR moves: %~2, Percent contracted: %~3, Instance: %~4

java RandomTree %~1 0 50 > A.txt
java -jar RandomRSPR.jar %~2 0 < A.txt > B.txt

java TreeContract %~3 0 < A.txt > A_NB.txt
java TreeContract %~3 0 < B.txt > B_NB.txt

type A_NB.txt B_NB.txt > Data/n%~1r%~2c%~3_%~4.txt

Exit /B 0