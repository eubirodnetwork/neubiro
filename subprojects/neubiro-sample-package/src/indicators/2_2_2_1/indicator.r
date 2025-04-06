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
source(paste(baseDir,"/2_2_2_1/implementation.r", sep=""))

# entry point
biro_2_2_2_1(
   id="2.2.2.1",
   id_file="2_2_2_1",

   xvar="SMOK_STAT",
   xvar_label="Smoking Status",
   x_levels=c("1","2","3"),
   x_levels_labs=c("Current Smoker","Non-Smoker","Ex-Smoker"),

   yvar="TYPE_DM",
   yvar_label="Type of Diabetes",
   y_levels=c("1","2","3"),
   y_levels_labs=c("Type 1","Type 2","Other"),

   footnote="Table 2.2.2.1 Smoking Status by Type of Diabetes",
   title="2.2.2.1 Smoking Status by Type of Diabetes",
   section="2.2.2.1 Smoking Status"
  )

rm(list=ls(all=TRUE))
