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

BIRO_report<- function(
 verbose=1,
 id="BIRO Report",
 chapter=c(""),
 sect1=list(c("")),
 sect2=list(list(c(""))),
 pile_up_id=list(list(list(c(""))))
 ) {

 if (verbose>0) {
  cat("########################################\r")
  if (language=="it") {
   cat(paste("Indicatore ,",id,": avvio elaborazione\r",sep=""))
  } else if (language=="en") {
   cat(paste("Indicator ",id,": started processing\r",sep=""))
  }
  cat("########################################\r")
 }

 # Set the working directory
 setwd(workDir)

 ################ Main BIRO dataset

 if (engine_type=="local")  {
  BIRO_Indicator_Prolog(verbose=1,id="1.1.1",id_file="1_1_1",infile=paste(workDir,"/../",pile_up_id[[1]][[1]][1],"/input.csv", sep=""),writecsv=0)
 }

 ################ BIRO Local Report

 # First empty row necessary to initialize output
 fileConn<-file("report.xml")
 writeLines("",fileConn)
 close(fileConn)

 fileConn<-file("report.xml",open="at")
 writeLines('<?dbhtml filename="EUBIROD_Report_intro.html" dir="html"?><title>General Population</title>',fileConn)
 writeLines('</chapter>',fileConn)
 close(fileConn)

 xml_id<-""

 for (k in 1:length(chapter)) { # New chapter

  xml_id<-paste(' xml:id="EUBIROD_Report_ch',k,'_s1','"',sep="")
  
  fileConn<-file("report.xml",open="at")
  writeLines(paste('<chapter',xml_id,'><?dbhtml dir="html"?>',sep=""),fileConn)
  writeLines(paste('<title>',chapter[k],'</title>',sep=""),fileConn)
  close(fileConn)

  for (j1 in 1:length(sect1[[k]])) { # New section 1

   if (j1>1) {
    xml_id<-paste(' xml:id="EUBIROD_Report_ch',k,'_s',j1,'"',sep="")
   } else {xml_id<-""}

   fileConn<-file("report.xml",open="at")
   writeLines(paste('<section',xml_id,'>',sep=""),fileConn)
   writeLines(paste('<title>',sect1[[k]][j1],'</title>',sep=""),fileConn)
   close(fileConn)

   for (j2 in 1:length(sect2[[k]][[j1]])) { # New section 2

    if (sect2[[k]][j1][j2]!="") {
     nextsect<-3
     fileConn<-file("report.xml",open="at")
     writeLines('<section>',fileConn)
     writeLines(paste('<title>',sect2[[k]][[j1]][j2],'</title>',sep=""),fileConn)
     close(fileConn)
    } else {
     nextsect<-2
    }

    for (i in 1:length(pile_up_id[[k]][[j1]][[j2]])) { # New section 2/3

     if (pile_up_id[[k]][[j1]][[j2]]!="") {

      fileConn<-file("report.xml",open="at")
      writeLines(paste('<section>',sep=""),fileConn)
      close(fileConn)

      file.append("report.xml",paste("../",pile_up_id[[k]][[j1]][[j2]][i],"/report.xml",sep=""))
   
      fileConn<-file("report.xml",open="at")
      writeLines(paste('</section>',sep=""),fileConn)
      close(fileConn)

      if (j1<length(sect1[[k]]))  {
       fileConn<-file("report.xml",open="at")
       writeLines('<?custom-pagebreak?>',fileConn)
       close(fileConn)
      }
    
     } # check on single indicator

    } # End subsection 2

    if (sect2[[k]][[j1]][j2]!="") {
     fileConn<-file("report.xml",open="at")
     writeLines('</section>',fileConn)
     close(fileConn) 
    }

   } # End subsection 1

   fileConn<-file("report.xml",open="at")
   writeLines('</section>',fileConn)
   close(fileConn)

  } # End section

  if (k<length(chapter)) {
   fileConn<-file("report.xml",open="at")
   writeLines('</chapter>',fileConn)
   close(fileConn)
  }

 } # End chapter

 ################ BIRO Local Report Output

 zipfiles<-c()

 for (k in 1:length(chapter)) {
  for (j1 in 1:length(sect1[[k]])) {
   for (j2 in 1:length(sect2[[k]][[j1]])) {
    for (i in 1:length(pile_up_id[[k]][[j1]][[j2]])) {

     if (pile_up_id[[k]][[j1]][[j2]][i]!="") {

      origin_files<-list.files(paste("../",pile_up_id[[k]][[j1]][[j2]][i],"/statistical_objects",sep=""),full.names=TRUE)

      for (m in 1:length(origin_files)) {
       dir.create(paste("statistical_objects/",pile_up_id[[k]][[j1]][[j2]][i],sep=""),recursive=TRUE)  
       file.copy(origin_files[m],paste(workDir,"/statistical_objects/",pile_up_id[[k]][[j1]][[j2]][i],sep=""))
      }

      target_files<-list.files(paste("statistical_objects/",pile_up_id[[k]][[j1]][[j2]][i],sep=""),full.names=TRUE)

      for (m in 1:length(target_files)) {
       zipfiles<-c(zipfiles,target_files[m])
      }

     } # check on origin_files

    }
   }
  }
 }

 # Build zip.yml according to the specifications internally passed to NeuBIRO
 fileConn<-file("descriptor.yml")
 writeLines("# BIRO STATISTICAL ENGINE OUTPUT DESCRIPTOR",fileConn)
 close(fileConn)
 fileConn<-file("descriptor.yml",open="at")
 writeLines(paste('operator: "',operator,'"',sep=""),fileConn)
 writeLines(paste('year: "',year,'"',sep=""),fileConn)
 writeLines(paste('select_unit: "',select_unit,'"',sep=""),fileConn)
 if (exists("selector")==TRUE) {
  writeLines(paste('selector: "',selector[1],"=",selector[2],'"',sep=""),fileConn)
 } else {
  writeLines('selector:',fileConn)
 }
 writeLines('type: "oracle"',fileConn)
 close(fileConn)

 assign("select_unit",select_unit,envir=.GlobalEnv)

 writeZipDescriptorFor("biro_report_output.zip",c("descriptor.yml",zipfiles))
 BIRO_Indicator_Epilog(verbose=1,id=id,id_file="")
 
}
