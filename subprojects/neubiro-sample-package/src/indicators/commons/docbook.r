###################################################################
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
# Author: Stefano Gualdi, stefano.gualdi@gmail.com
####################################################################

####################################################################
# openReportXml()
#
# Starts writing the report.xml file
####################################################################

openReportXml <- function(append = FALSE) {
  file <- "report.xml"
  if (!append) {
    # Reset the file
    fileConn <- file(file)
    writeLines("", fileConn)
    close(fileConn)
  }
  fileConn <- file(file, open = "at")

  fileConn
}

####################################################################
# closeReportXml()
#
# Closes the report.xml file
####################################################################

closeReportXml <- function(fileConn) {
  close(fileConn)
}

####################################################################
# writeTitle()
#
# Write a simple docbook title paragraph
####################################################################

writeTitle <- function(title = "", subtitle = "", fileConn) {

  if(title != "") {
    writeLines(paste("<title>", title, "</title>", sep = ""), fileConn)
  }

  if (subtitle != "") {
    writeLines('<para>', fileConn)
    writeLines(paste('<emphasis role="red">', subtitle, '</emphasis>', sep = ""), fileConn)
    writeLines('</para>', fileConn)
  }
}

####################################################################
# writeGraph()
#
# Write a simple docbook graph object
####################################################################

writeGraph <- function(graphFilename, width, fileConn) {
  writeLines('<para>', fileConn)
  writeLines('<mediaobject>', fileConn)
  writeLines('<imageobject>', fileConn)
  writeLines(
    paste(
      '<imagedata align="center" fileref="',
      graphFilename,
      '" scalefit="1" width="',
      width,
      '%" contentdepth="100%" format="PDF"/>',
      sep = ""
    ),
    fileConn
  )
  writeLines('</imageobject>', fileConn)
  writeLines('</mediaobject>', fileConn)
  writeLines('</para>', fileConn)
}

####################################################################
# writeTable()
#
# Write a docbook table
#
# arguments:
####################################################################

writeTable <- function(file="",
                       tablerole="",
                       append=1,
                       data=NULL,

                       xvar="",
                       xvar_label="",
                       x_levels="",
                       x_levels_labs="",

                       yvar="",
                       yvar_label="",
                       y_levels="",
                       y_levels_labs="",

                       zvar="",
                       zvar_label="",
                       z_levels="",
                       z_levels_labs="",

                       vars="",
                       headlabs="",
                       headwidt="",
                       colalign="",
                       varcolalign="",
                       headalign=NULL,
                       footlabs="",
                       title="",
                       section="",
                       subtitle="",
                       footnote=NULL,
                       graph=NULL,
                       graph_width=100) {
					   
 if (append==0) {
  fileConn<-file(file)
  writeLines("",fileConn)
  close(fileConn)
 }

 fileConn<-file(file,open="at")

 if (zvar=="") {
  n_z_levels<-1
  n_z_header<-2
 } else {
  n_z_header<-4
  n_z_levels<-length(z_levels)
 }
  
 if (yvar=="") {
  n_y_levels<-0
 } else {
  n_y_levels<-length(y_levels)
 } 

 ncol<-n_z_levels*n_y_levels*2+2+1

 if (!is.null(data)==TRUE) {

  table <- data

  # Write report section in xml
  if (subtitle!="") {
   writeLines('<para>',fileConn)
   writeLines(paste('<emphasis role="red">',subtitle,'</emphasis>',sep=""),fileConn)
   writeLines('</para>',fileConn)
  }
  writeLines('<para>',fileConn)
  writeLines("",fileConn)

  if (tablerole!="") {
  writeLines(paste(' <table role="',tablerole,'" frame="none">',sep=""),fileConn)
  } else {
  writeLines(' <table frame="none">',fileConn)
  }
  writeLines("",fileConn)
  writeLines(paste('  <title>',title,'</title>',sep=""),fileConn)
  writeLines("",fileConn)
  writeLines(paste('  <tgroup cols="',length(vars),'" align="left" colsep="1" rowsep="1">',sep=""),fileConn)

  writeLines("",fileConn)
  for(i in 1:length(headlabs)){
   if (colalign[1]!="") {
    writeLines(paste('     <colspec colname="',headlabs[i],'"',' colnum="',i,'" colwidth="',headwidt[i],'"',' align="',colalign[i],'"/>',sep=""),fileConn)
   } else {
    writeLines(paste('     <colspec colname="',headlabs[i],'"',' colnum="',i,'" colwidth="',headwidt[i],'"/>',sep=""),fileConn)
   }
  }
  writeLines("",fileConn)

  # If you repeatedly use the same span in a table, you can specify a named spanspec element in tgroup to pre declare the start and end column names. 
  # Then an entry can just refer to the named spanspec, using one attribute instead of two (and reducing the possibility of error). 

  if (zvar!="") {
   writeLines(paste('     <spanspec spanname="zvar" namest="X2" nameend="X',ncol-2,'"/>',sep=""),fileConn)
   for(i in 1:n_z_levels){
    writeLines(paste('     <spanspec spanname="zvar_lev',"_",i,'" namest="X',(i-1)*n_y_levels*2+2,'" nameend="X',i*n_y_levels*2+1,'"/>',sep=""),fileConn)
   }
  }

  if (yvar!="") {

   k<-0

   writeLines(paste('     <spanspec spanname="preamble" namest="X1" nameend="X1"/>',sep=""),fileConn)
   writeLines(paste('     <spanspec spanname="yvar" namest="X2" nameend="X',ncol-2,'" align="center"/>',sep=""),fileConn)

   for(j in 1:n_z_levels){
    for(i in 1:n_y_levels){
     k<-k+1
     writeLines(paste('     <spanspec spanname="yvar_lev',"_",k,'" namest="X',k*2,'" nameend="X',k*2+1,'" align="center"/>',sep=""),fileConn)
    }
   }
   writeLines(paste('     <spanspec spanname="postamble" namest="X',ncol-1,'" nameend="X',ncol,'" align="center"/>',sep=""),fileConn)
  }
  writeLines("",fileConn)

  if (!is.null(footlabs)==TRUE) {
   writeLines('   <thead>',fileConn)
   writeLines('    <row>',fileConn)
   for(i in 1:length(footlabs)){ 

    if (!is.null(headalign)==TRUE) {
     writeLines(paste('     <entry align="',headalign[i],'">',footlabs[i],'</entry>',sep=""),fileConn)
    } else {
     writeLines(paste('     <entry>',footlabs[i],'</entry>',sep=""),fileConn)
    }
   }
   writeLines('    </row>',fileConn)
   writeLines('   </thead>',fileConn)
  }
  
  writeLines("",fileConn)

  # Data table

  writeLines('   <tbody>',fileConn)

  if (zvar!="") {

   # zvar label

   writeLines(paste('    <row>',sep=""),fileConn)
   writeLines(paste('     <entry colsep="1" align="center" valign="middle" morerows="',n_z_header,'"><emphasis role="strong">',toupper(xvar_label),'</emphasis></entry>',sep=""),fileConn)
   writeLines(paste('     <entry colsep="1" align="center" spanname="zvar" ><?dbhtml bgcolor="#FFB266"?><emphasis role="strong">',toupper(zvar_label),'</emphasis><?dbfo bgcolor="#FFB266" ?></entry>',sep=""),fileConn)
   writeLines(paste('     <entry rowsep="0" colsep="0" spanname="postamble" align="center" valign="middle"></entry>',sep=""),fileConn)         
   writeLines(paste('    </row>',sep=""),fileConn)

   # zvar levels
   
   writeLines(paste('    <row>',sep=""),fileConn)
   for(i in 1:n_z_levels){
    writeLines(paste('     <entry spanname="zvar_lev',"_",i,'" align="center" valign="middle"><?dbhtml bgcolor="#FFFFCC"?><emphasis role="strong">',z_levels_labs[i],'</emphasis><?dbfo bgcolor="#FFFFCC" ?></entry>',sep=""),fileConn)
   }
   writeLines(paste('     <entry rowsep="0" colsep="0" spanname="postamble" align="center" valign="middle"></entry>',sep=""),fileConn)         
   writeLines(paste('    </row>',sep=""),fileConn)

  } # zvar

  if (yvar!="") {

   k<-0

   # yvar label

   writeLines(paste('    <row>',sep=""),fileConn)
   if (zvar=="") {
    writeLines(paste('     <entry colsep="1" align="center" valign="bottom" morerows="',n_z_header,'"><emphasis role="strong">',toupper(xvar_label),'</emphasis></entry>',sep=""),fileConn)
   }
   writeLines(paste('     <entry colsep="1" spanname="yvar" ><?dbhtml bgcolor="#FFB266"?><emphasis role="strong">',toupper(yvar_label),'</emphasis><?dbfo bgcolor="#FFB266" ?></entry>',sep=""),fileConn)
   writeLines(paste('     <entry rowsep="0" colsep="0" spanname="postamble" align="center" valign="middle"></entry>',sep=""),fileConn)         
   writeLines(paste('    </row>',sep=""),fileConn)

   # yvar levels
   writeLines(paste('    <row>',sep=""),fileConn)
   for(j in 1:n_z_levels){
    for(i in 1:n_y_levels){
     k<-k+1
#     print(k)
     writeLines(paste('     <entry spanname="yvar_lev',"_",k,'" align="center" valign="middle"><?dbhtml bgcolor="#FFFFCC"?><emphasis role="strong">',y_levels_labs[i],'</emphasis><?dbfo bgcolor="#FFFFCC" ?></entry>',sep=""),fileConn)
    }
   }
   writeLines(paste('     <entry rowsep="0" colsep="0" spanname="postamble" align="center" valign="middle"></entry>',sep=""),fileConn)
   writeLines(paste('    </row>',sep=""),fileConn)

  }

  # xvar headers

  writeLines(paste('    <row>',sep=""),fileConn)
  if (zvar=="" & yvar=="") {
   writeLines(paste('     <entry colsep="1" align="center" valign="middle"><emphasis role="strong">',toupper(xvar_label),'</emphasis></entry>',sep=""),fileConn)
  }
  if (yvar!="") {
   for(j in 1:(n_z_levels)){
    for(i in 1:(n_y_levels)){
     writeLines(paste('     <entry colsep="0" align="center" valign="middle" >N</entry>',sep=""),fileConn)
     writeLines(paste('     <entry colsep="1" align="center" valign="middle" >%</entry>',sep=""),fileConn)
    }
   }
  }
  writeLines(paste('     <entry colsep="0" align="center" valign="middle" >N</entry>',sep=""),fileConn)
  writeLines(paste('     <entry colsep="1" align="center" valign="middle" >%</entry>',sep=""),fileConn)
  writeLines(paste('    </row>',sep=""),fileConn)

  # Data

  if (zvar!="") {
   firstobs<-4
  } else {
   if (yvar!="") {
    firstobs<-2
   } else {
    firstobs<-2
   }
  }

  for(j in firstobs:nrow(table)){

   writeLines("",fileConn)
   writeLines('    <row>',fileConn)

   if (j<nrow(table)) {
    for(k in 1:(length(vars))){
     if (k==1) {
      if (varcolalign[1]!="") {
       writeLines(paste('     <entry align="',table[j,varcolalign[k]],'"><?dbhtml bgcolor="#FFFFCC"?><emphasis role="strong">',table[j,vars[k]],'</emphasis><?dbfo bgcolor="#FFFFCC" ?></entry>',sep=""),fileConn)
      } else {
       writeLines(paste('     <entry align="center"><?dbhtml bgcolor="#FFFFCC"?><emphasis role="strong">',table[j,vars[k]],'</emphasis><?dbfo bgcolor="#FFFFCC" ?></entry>',sep=""),fileConn)
      }
     } else if (k==(length(vars)-1)) {
      writeLines(paste('     <entry colsep="0">',table[j,vars[k]],'</entry>',sep=""),fileConn)
     } else {
      writeLines(paste('     <entry>',table[j,vars[k]],'</entry>',sep=""),fileConn)
     }
    }
   } else {
    for(k in 1:(length(vars))){
     if (k==1) {
      if (varcolalign[1]!="") {
       writeLines(paste('     <entry colsep="0" align="',table[j,varcolalign[k]],'"><emphasis role="strong">',table[j,vars[k]],'</emphasis></entry>',sep=""),fileConn)
      } else {
       writeLines(paste('     <entry align="center"><emphasis role="strong">',table[j,vars[k]],'</emphasis></entry>',sep=""),fileConn)
      }
     } else if (k>1 & k<length(vars)) {
      writeLines(paste('     <entry colsep="0"><emphasis role="strong">',table[j,vars[k]],'</emphasis></entry>',sep=""),fileConn)
     } else if (k==length(vars)) {
      writeLines(paste('     <entry ><emphasis role="strong">',table[j,vars[k]],'</emphasis></entry>',sep=""),fileConn)
     }
    }
   }
   writeLines('    </row>',fileConn)
  }

  writeLines("",fileConn)
  writeLines('   </tbody>',fileConn)
  writeLines('  </tgroup>',fileConn)
  writeLines(' </table>',fileConn)
  writeLines("",fileConn)
  writeLines('</para>',fileConn)

 }

 writeLines("",fileConn)
 writeLines(paste('<para text-align="center"></para>',sep=""),fileConn)

 if (!is.null(footnote)==TRUE) {
  writeLines(paste('<para text-align="center">',footnote,'</para>',sep=""),fileConn)
 }

 if (!is.null(graph)==TRUE) {
  for (q in 1:length(graph)) {
   writeLines('<para>',fileConn)
   writeLines(' <mediaobject>',fileConn)
   writeLines('  <imageobject>',fileConn)
   writeLines(paste('   <imagedata align="center" fileref="',graph[q],'" scalefit="1" width="',graph_width,'%" contentdepth="100%" format="PDF"/>',sep=""),fileConn)
   writeLines('  </imageobject>',fileConn)
   writeLines(' </mediaobject>',fileConn)
   writeLines('</para>',fileConn)
   writeLines('<para></para>',fileConn)
  }
 }

 close(fileConn)
 
}
