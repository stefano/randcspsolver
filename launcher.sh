#!/bin/sh

echo "" > output.csv;

echo -e "\"Number of variables: 1-10\"\n" >> output.csv;
for n in {1,2,3,4,5,6,7,8,9,10};
do java Solver -n $n -l 5 -d 0.5 -s 0.5 -b 50 >> output.csv;
done

echo -e "\n\"Number of variables: 1-8 (with AC)\"\n" >> output.csv;
for n in {1,2,3,4,5,6,7,8,9,10};
do java Solver -n $n -l 5 -d 0.5 -s 0.5 -b 50 -ac >> output.csv;
done

echo -e "\n\"Cardinality of domains: 1-10\"\n" >> output.csv;
for l in {1,2,3,4,5,6,7,8,9,10};
do java Solver -n 5 -l $l -d 0.5 -s 0.5 -b 50 >> output.csv;
done

echo -e "\n\"Cardinality of domains: 1-10 (with AC)\"\n" >> output.csv;
for l in {1,2,3,4,5,6,7,8,9,10};
do java Solver -n 5 -l $l -d 0.5 -s 0.5 -b 50 -ac >> output.csv;
done

echo -e "\n\"Density: 0.1-1\"\n" >> output.csv;
for d in {0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1};
do java Solver -n 5 -l 5 -d $d -s 0.5 -b 50 >> output.csv;
done

echo -e "\n\"Density: 0.1-1 (with AC)\"\n" >> output.csv;
for d in {0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1};
do java Solver -n 5 -l 5 -d $d -s 0.5 -b 50 -ac >> output.csv;
done

echo -e "\n\"Strictness: 0.1-1\"\n" >> output.csv;
for s in {0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1};
do java Solver -n 5 -l 5 -d 0.5 -s $s -b 50 >> output.csv;
done

echo -e "\n\"Strictness: 0.1-1 (with AC)\"\n" >> output.csv;
for s in {0.05,0.1,0.15,0.2,0.25,0.3,0.35,0.4,0.45,0.5,0.55,0.6,0.65,0.7,0.75,0.8,0.85,0.9,0.95,1};
do java Solver -n 5 -l 5 -d 0.5 -s $s -b 50 -ac >> output.csv;
done

