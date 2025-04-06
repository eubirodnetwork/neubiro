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
source(paste(baseDir,"/2_1_2/implementation.r", sep=""))

# entry point
biro_2_1_2(
   id="2.1.2",
   id_file="2_1_2",
   xvar="CL_DIAB_DUR",
   xvar_label="Duration of Diabetes",
   x_levels=c("1","2","3"),
   x_levels_labs=c("&lt;10","[10-20)","&gt;=20"),
        
   yvar="TYPE_DM",
   yvar_label="Type of Diabetes",
   y_levels=c("1","2","3"),
   y_levels_labs=c("Type 1","Type 2","Other"),

   footnote="Table 2.1.2 Duration of Diabetes by Type of Diabetes",
   title="2.1.2 Duration of Diabetes by Type of Diabetes",
   section="2.1.2 Duration of Diabetes"
  )

rm(list=ls(all=TRUE))
