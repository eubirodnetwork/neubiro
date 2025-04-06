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
source(paste(baseDir,"/1_1_1/implementation.r", sep=""))

# entry point
biro_1_1_1(
   id="1.1.1",
   id_file="1_1_1",

   zvar="CL_AGE",
   zvar_label="Age",
   z_levels=c("1","2","3","4","5","6"),
   z_levels_labs=c("&lt;15","[15-45)","[45-55)","[55,65)","[65-75)","&gt;=75"),

   yvar="SEX",
   yvar_label="Gender",
   y_levels=c("M","F"),
   y_levels_labs=c("Male","Female"),

   xvar="TYPE_DM",
   xvar_label="Type of Diabetes",
   x_levels=c("1","2","3"),
   x_levels_labs=c("Type 1","Type 2","Other"),

   footnote="Table 1.1.1 Age by Gender",
   title="1.1.1 Age by Gender",
   section="Indicator 1.1.1 Age"
  )

rm(list=ls(all=TRUE))
