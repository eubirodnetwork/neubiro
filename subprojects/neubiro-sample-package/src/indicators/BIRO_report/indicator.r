####################################################################
# Copyright 2016 Fabrizio Carinci, Stefano Gualdi, EUBIROD Network.
#
# Licensed under the European Union Public Licence (EUPL), Version 1.1 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#       http://joinup.ec.europa.eu/software/page/eupl/licence-eupl
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
####################################################################

####################################################################
# Author: Fabrizio Carinci, fabcarinci@gmail.com
# Author: Stefano Gualdi, stefano.gualdi@gmail.com
####################################################################

# =====================================================
# Entry point
#
# baseDir = Root directory for all indicators
# workDir = Work directory for this indicator
#
# =====================================================

# Load common functions
source(paste(baseDir, "/commons/options.r", sep=""))
source(paste(baseDir, "/commons/tools.r", sep=""))
source(paste(baseDir, "/commons/docbook.r", sep=""))

# Load implementation
source(paste(baseDir,"/BIRO_report/implementation.r", sep=""))

BIRO_report( 
 chapter=c("1. Demographic characteristics",
           "2. Clinical characteristics",
           "3. Health system",
           "4. Population",
           "5. Risk Adjusted Indicators",
           "6. Pediatric Section"),
 sect1=list(c("1.1. Basic Demographics"),
            c("2.1. Diabetes Status","2.2. Risk factors","2.3. Diabetes Complications"),
            c("3.1. Structures","3.2. Structural Quality","3.3. Processes"),
            c("4.1. Vital Statistics"),
            c("5.1. Epidemiology","5.2. Process Quality","5.3. Outcome Quality - Intermediate Outcomes","5.4. Outcome Quality - Terminal Outcomes"),
            c("6.1. Pediatric Demographics","6.2. Diabetes Status","6.3. Risk Factors","6.4. Clinical Measurements")),
 sect2=list(list(c("")),
            list(c(""),c("2.2.1. Obesity","2.2.2. Lifestyle","2.2.3. Clinical Measurements"),c("")),
            list(c(""),c(""),c("")),
            list(c("")),
            list(c(""),c(""),c(""),c("")),
            list(c(""),c(""),c(""),c(""))
            ),
 pile_up_id=list(list( list(c("1.1.1")) ),
                 list( list(c("2.1.1","2.1.2")),list(c("2.2.1.1","2.2.1.2"),c("2.2.2.1"),c("2.2.3.1","2.2.3.2","2.2.3.3","2.2.3.4","2.2.3.5","2.2.3.6")),list(c("2.3.4")) ),
                 list( list(c("")),list(c("")),list(c("")) ),
                 list( list(c("")) ),
                 list( list(c("")),list(c("5.2.4")),list(c("5.3.1","5.3.2","5.3.4","5.3.6")),list(c("")) ),
                 list( list(c("")),list(c("")),list(c("")),list(c("")))
                 )
)

rm(list=ls(all=TRUE))
