# LICENSE
# Copyright (c) 2013-2016, Jesse Hostetler (jessehostetler@gmail.com)
# All rights reserved.
# 
# Redistribution and use in source and binary forms, with or without
# modification, are permitted provided that the following conditions are met:
# 
# 1. Redistributions of source code must retain the above copyright notice,
# this list of conditions and the following disclaimer.
# 2. Redistributions in binary form must reproduce the above copyright notice,
# this list of conditions and the following disclaimer in the documentation
# and/or other materials provided with the distribution.
# 
# THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
# AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
# IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
# DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE
# FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
# DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
# SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
# CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
# OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
# OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

# Based on:
# http://www.hep.ph.ic.ac.uk/~dbauer/grid/myqstat.py

import xml.dom.minidom
import os
import sys
import string
#import re


f=os.popen('qstat -u hostetje -xml -r')

dom=xml.dom.minidom.parse(f)


jobs=dom.getElementsByTagName('job_info')
run=jobs[0]

runjobs=run.getElementsByTagName('job_list')


def fakeqstat(joblist):
        for r in joblist:
                jobname=r.getElementsByTagName('JB_name')[0].childNodes[0].data
                jobown=r.getElementsByTagName('JB_owner')[0].childNodes[0].data
                jobstate=r.getElementsByTagName('state')[0].childNodes[0].data
                jobnum=r.getElementsByTagName('JB_job_number')[0].childNodes[0].data
                jobtime='not set'
                if(jobstate=='r'):
                        jobtime=r.getElementsByTagName('JAT_start_time')[0].childNodes[0].data
                elif(jobstate=='dt'):
                        jobtime=r.getElementsByTagName('JAT_start_time')[0].childNodes[0].data
                else:
                        jobtime=r.getElementsByTagName('JB_submission_time')[0].childNodes[0].data



                print jobnum, '\t', jobown, '\t', jobname,'\t', jobstate,'\t',jobtime


fakeqstat(runjobs)

