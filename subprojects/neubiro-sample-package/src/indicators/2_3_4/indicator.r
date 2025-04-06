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
source(paste(baseDir,"/2_3_4/implementation.r", sep=""))

# entry point
biro_2_3_4(
   id="2.3.4",
   id_file="2_3_4",

   xvar="AMPUT",
   xvar_label="Lower extremity amputation",
   x_levels=c("0","1"),
   x_levels_labs=c("No","Yes"),

   yvar="CL_DIAB_DUR",
   yvar_label="Duration of Diabetes",
   y_levels=c("1","2","3"),
   y_levels_labs=c("&lt;10","[10-20)","&gt;=20"),

   footnote="Table 2.3.4 Lower extremity amputation by Duration of Diabetes",
   title="2.3.4 Lower extremity amputation by Duration of Diabetes",
   section="2.3.4 Lower extremity amputation by Duration of Diabetes"
  )

rm(list=ls(all=TRUE))
