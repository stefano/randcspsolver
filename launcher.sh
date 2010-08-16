#!/bin/sh

echo "" > output.csv;

echo -e "\"Number of variables: 1-5\"\n" >> output.csv;
for n in {1,2,3,4,5};
do java Solver -n $n -l 3 -d 0.5 -s 0.5 >> output.csv;
done

echo -e "\n\"Length of domains: 1-5\"\n" >> output.csv;
for l in {1,2,3,4,5};
do java Solver -n 3 -l $l -d 0.5 -s 0.5 >> output.csv;
done

echo -e "\n\"Density: 0.1-1\"\n" >> output.csv;
for d in {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
do java Solver -n 3 -l 3 -d $d -s 0.5 >> output.csv;
done

echo -e "\n\"Strictness: 0.1-1\"\n" >> output.csv;
for s in {0.1,0.2,0.3,0.4,0.5,0.6,0.7,0.8,0.9,1};
do java Solver -n 3 -l 3 -d 0.5 -s $s >> output.csv;
done

