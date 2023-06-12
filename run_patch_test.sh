#!/bin/bash

cd $HOME/spark

result_file=raw_result_before

if [ -d $result_file ]; then
    rm -f $result_file
fi

for rep in 1 2 3
do
    sbt "sql/testOnly *InsertSuite -- -z SPARK-29938" &> raw_output.log
    grep -F '!!!' raw_output.log >> $result_file
done

rm raw_output.log
mv $result_file $HOME