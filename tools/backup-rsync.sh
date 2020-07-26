#!/bin/bash
#
# clean test data
#
# exclude generated output files, test dirs, blob dirs and backup files
#
# call me from deltachat base dir  

rm -r -f ~/tmp/deltachat-backup/*
mkdir ~/tmp/deltachat-backup

cd ..
rsync -a --progress deltachat-android/ ~/tmp/deltachat-backup --exclude="*.db" --exclude="target" --exclude="*-blobs" --exclude="*.bak"
cd deltachat-android



