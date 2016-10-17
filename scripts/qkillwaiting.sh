#!/bin/bash

waiting=$(qjobs | cut -f 1,3,4 | grep 'qw[[:space:]]*$' | cut -f 1)
echo $waiting
