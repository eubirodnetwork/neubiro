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
source(paste(baseDir,"/5_2_4/implementation.r", sep=""))

# entry point
biro_5_2_4(
   id="5.2.4",
   id_file="5_2_4",

   xvar="FOOT_EXAM",
   xvar_label="Foot examination",
   x_levels=c("0","1"),
   x_levels_labs=c("No","Yes"),

   yvar="TYPE_DM",
   yvar_label="Type of Diabetes",
   y_levels=c("1","2","3"),
   y_levels_labs=c("Type 1","Type 2","Other"),

   footnote="Table 5.2.4 Foot examination by Type of Diabetes",
   title="5.2.4 Foot examination by Type of Diabetes",
   section="5.2.4 Foot examination by Type of Diabetes"
  )

rm(list=ls(all=TRUE))
