####################################################################
# Copyright 2016 Fabrizio Carinci, EUBIROD Network.
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
####################################################################

biro_2_2_2_1 <- function(
   verbose=1,
   id="",
   id_file="",
   xvar="",
   xvar_label="",
   x_levels=c(),
   x_levels_labs=c(),

   yvar="",
   yvar_label="",
   y_levels=c(),
   y_levels_labs=c(),

   zvar="",
   zvar_label="",
   z_levels=c(),
   z_levels_labs=c(),

   footnote="",
   title="",
   section=""
 ) {

 BIRO_Indicator_Prolog(verbose=1,title=title,fileconn="report.xml",
                       id=id,id_file=id_file,infile=paste(workDir,"/input.csv",sep=""))

 NeuBIRO_Categorical(
   file="report.xml",
   append=1,
   data=input_data,
   xvar=xvar,
   xvar_label=xvar_label,
   x_levels=x_levels,
   x_levels_labs=x_levels_labs,

   yvar=yvar,
   yvar_label=yvar_label,
   y_levels=y_levels,
   y_levels_labs=y_levels_labs,

   zvar=zvar,
   zvar_label=zvar_label,
   z_levels=z_levels,
   z_levels_labs=z_levels_labs,

   footnote=footnote,
   title=title,
   section=section
   
   ) 

 BIRO_Indicator_Epilog(verbose=1,id=id,id_file=id_file)

}
