####################################################################
# Copyright 2017 Fabrizio Carinci, EUBIROD Network
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
# Author: Fabrizio Carinci, EUBIROD Network, fabcarinci@gmail.com
# November 2019
####################################################################

liste_opzioni<-function(installed=1)

{

 # Print all warnings
 options(warn=1)

 # Suppress scientific notation
 options(scipen=999)

 ############## set by neubiro at startup

# language<-"it"
# engine_type<-"local"
# operator<-"Fab"
# year<-"2012"
# select_unit=""
# reference<-""

 if (language=="it") {
  language="italian"
  bigmark="."
  decimalmark=","
  target_name<-"Fonte dati"
 } else if (language=="en") {
  language="english"
  bigmark=","
  decimalmark="."
  target_name<-"Data Source"
 }

 if (select_unit=="" & reference=="_internal_") {reference=""}

 output_file<-"tracciato_record_centrale"

# ###################################################

 assign("language",language,envir=.GlobalEnv)
 assign("engine_type",engine_type,envir=.GlobalEnv)
 assign("operator",operator,envir=.GlobalEnv)
 assign("year",year,envir=.GlobalEnv)
 assign("funnel_group",funnel_group,envir=.GlobalEnv)
 assign("select_unit",select_unit,envir=.GlobalEnv)
 assign("reference",reference,envir=.GlobalEnv)

 assign("output_file",output_file,envir=.GlobalEnv)
 assign("input_files",input_files,envir=.GlobalEnv)

# ############## EUBIROD specific

 global_system_levels<-c("COUNTRY")
 pop_levels<-c("DS_ID")
 local_system_levels<-c("SUB_DS_ID")
 classvar<-c("TYPE_DM")
 adjusters<-c("SEX","CL_AGE","CL_DIAB_DUR")
 list_complications<-c("RETINA","AMPUT","ULCER","DIALYSIS","MI")

 list_merge=c(pop_levels,local_system_levels,classvar,adjusters,list_complications)

 target<-"SUB_DS_ID"  # output target for funnel plot data

 if (engine_type=="local") {
  funnel_unit<-"SUB_DS_ID"
  dot_size<-0.9
  if (language=="italian") {
   funnel_unit_name<-"Centro"
  } else if (language=="english") {
   funnel_unit_name<-"Centre"
  }
 } else if (engine_type=="central") {
  funnel_unit<-"DS_ID"
  dot_size<-1.8
  if (language=="italian") {
   funnel_unit_name<-"Regione"
  } else if (language=="english") {
   funnel_unit_name<-"Region"
  }
 }

# ############### assign needed

 assign("bigmark",bigmark,envir=.GlobalEnv)
 assign("decimalmark",decimalmark,envir=.GlobalEnv)

 assign("pop_levels",pop_levels,envir=.GlobalEnv)
 assign("local_system_levels",local_system_levels,envir=.GlobalEnv)
 assign("global_system_levels",global_system_levels,envir=.GlobalEnv)
 assign("adjusters",adjusters,envir=.GlobalEnv)
 assign("list_merge",list_merge,envir=.GlobalEnv)
 assign("classvar",list_merge,envir=.GlobalEnv)
 assign("funnel_unit",funnel_unit,envir=.GlobalEnv)
 assign("dot_size",dot_size,envir=.GlobalEnv)
 assign("funnel_unit_name",funnel_unit_name,envir=.GlobalEnv)
 assign("target",target,envir=.GlobalEnv)
 assign("target_name",target_name,envir=.GlobalEnv)
 assign("list_complications",list_complications,envir=.GlobalEnv)

 assign("PkgsNeed",PkgsNeed,envir=.GlobalEnv)

}
