#!/bin/bash

cd $HOME/spark

# compile spark
sbt package > /dev/null

result_file=raw_result_after

if [ -d $result_file ]; then
    rm -f $result_file
fi

for rep in 1 2 3
do
    bin/spark-shell --master 'local[16]' --driver-memory 8g < $HOME/spark-25947-reproducer.scala  > raw_output.log 2>&1 
    grep -F '!!!' raw_output.log >> $result_file
done

rm raw_output.log
mv $result_file $HOME
