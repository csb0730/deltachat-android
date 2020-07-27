#!/bin/bash
#
# clean test data
#
# exclude generated output files, test dirs, blob dirs and backup files
#
# call me from deltachat base dir  


tar --exclude="*.db" --exclude="target" --exclude="*-blobs" --exclude="*.bak" --exclude="build" -zcvpf ~/tmp/deltachat-backup-$(date +%Y-%m-%dT%H-%M).tar.gz ../deltachat-android



