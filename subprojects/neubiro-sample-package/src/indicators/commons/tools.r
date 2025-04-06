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

# NOTA DA STEFANO
# def runningParams = [
#            language       : model.language ?: NeubiroConfig.DEFAULT_LANGUAGE,
#            operator       : model.operatorName ?: "000000",
#            year           : model.year ?: "0000",
#            engine_type    : definedEngineType,
#            reference      : definedReferenceType,
#            reference_files: definedReferenceType == '_external_' ? model.referenceFilesList?.collect { it.name } : [],
#            input_files    : definedEngineType == 'central' ? model.inputFilesList?.collect { it.name } : [],
#            funnel_group   : highlights[0]?.value ?: ''
#          ]
#sono in NeubiroController.geroovy nel metodo runIndicators()

####################################################################
# Author: Fabrizio Carinci, fabcarinci@gmail.com
# Author: Stefano Gualdi, stefano.gualdi@gmail.com
####################################################################

####################################################################
# Check for installed packages
####################################################################

is.installed <- function(mypkg) is.element(mypkg, installed.packages()[, 1])

####################################################################
# installLibraries()
####################################################################

installLibraries <- function(lib) {
  lib_path <- .libPaths()[1]

  update.packages(ask = FALSE,
                  checkBuilt = TRUE,
                  repos = "http://cran.r-project.org",
                  lib = lib_path)

  for (i in 1:length(lib)) {
    if (!is.installed(lib[i])) {
      install.packages(lib[i], repos = "http://cran.r-project.org", lib = lib_path)
    }
    suppressMessages(library(lib[i], character.only = TRUE))
  }
}

make_numeric <- function(dataset, vars) {
  for (i in 1:length(vars)) {
    dataset[, vars[i]] <- as.numeric(dataset[, vars[i]])
    dataset[, vars[i]] <-
      ifelse(is.na(dataset[, vars[i]]), 0, dataset[, vars[i]])
  }
  dataset
}

make_character <- function(dataset, vars) {
  for (i in 1:length(vars)) {
    dataset[, vars[i]] <- as.character(dataset[, vars[i]])
    dataset[, vars[i]] <-
      ifelse(is.na(dataset[, vars[i]]), "", dataset[, vars[i]])
  }
  dataset
}

make_numeric_logical <- function(dataset, vars) {
  for (i in 1:length(vars)) {
    dataset[, vars[i]] <-
      as.numeric(as.logical(toupper(dataset[, vars[i]])))
  }
  dataset
}

####################################################################
# writeZipDescriptorFor()
#
# creates zip file descriptor
#
# arguments:
# zipfilename = name of the zip file to create
# filelist = array containing the filenames theat will compose the compressed archives
# cleanup = if TRUE delete components files after zip creation
####################################################################

writeZipDescriptorFor <- function(
 zipfilename,
 fileslist,
 cleanup = TRUE) {

 descfilename <- paste(zipfilename, ".yml", sep = "")
 fileConn <- file(descfilename)
 writeLines("# ZIP FILE DESCRIPTOR", fileConn)
 close(fileConn)
 fileConn <- file(descfilename, open = "at")
 writeLines(paste('file: ', zipfilename, sep = ""), fileConn)
 writeLines(paste('cleanup: ',ifelse(cleanup,'true','false'),sep =""), fileConn)
 writeLines('files:', fileConn)
 for (i in 1:length(fileslist)) {
   writeLines(paste('- ', fileslist[i], sep = ""), fileConn)
 }
 close(fileConn)

}

####################################################################
# NeuFreq()
#
# scope
#
# arguments:
####################################################################

NeuFreq<-function(file=file,
                  data=NULL,
                  xvar="",            # Table Rows
                  xvar_label="",
                  x_levels=c(),
                  x_levels_labs="",
                  yvar="",            # Table Columns
                  yvar_label="",
                  y_levels=c(),
                  y_levels_labs="",
                  zvar="",
                  zvar_label="",
                  z_levels=c(),
                  z_levels_labs="",
                  footnote="Table x.x - Footnote",
                  title="x.x.x.x Variable_Name",
                  section="Section x.x.x Section_Title",
                  append=0
                  ) {

  n_x_levels<-length(x_levels)
  n_y_levels<-length(y_levels)

  if (length(z_levels)==0) {
   n_z_levels<-1
   n_z_header<-0
  } else {
   n_z_levels<-length(z_levels)
   n_z_header<-2
  }

  table1 <- data.frame(matrix(ncol=n_z_levels*n_y_levels*2+3, nrow=n_z_header+n_x_levels+2))

  # Create the table
  data_nona <- data[data[,c(xvar)]!="",]

  # Upper Left
  table1[1,1] <- ""
  table1[2,1] <- ""
  table1[n_z_header+1,1] <- xvar_label
  # Lower left
  table1[n_z_header+n_x_levels+2,1] <- "TOTAL"
  # Lower right
  table1[n_z_header+n_x_levels+2,n_z_levels*n_y_levels*2+3]<-"100.0"

  # Upper right
  table1[n_z_header+1,n_z_levels*n_y_levels*2+2]<-"N"
  table1[n_z_header+1,n_z_levels*n_y_levels*2+3]<-"%"

# print(n_y_levels)
  
  # Table Header 
  if (n_y_levels>0) {
   for (z in 1:n_z_levels) {
    for (j in 1:n_y_levels) {
     if (zvar!="") {
      table1[1,(z-1)*n_y_levels*2+j*2]   <- zvar_label
      table1[1,(z-1)*n_y_levels*2+j*2+1] <- zvar_label
      table1[2,(z-1)*n_y_levels*2+j*2]   <- z_levels_labs[z]
      table1[2,(z-1)*n_y_levels*2+j*2+1] <- z_levels_labs[z]
     }
      table1[n_z_header+1,(z-1)*n_y_levels*2+j*2] <- y_levels_labs[j]
      table1[n_z_header+1,(z-1)*n_y_levels*2+j*2+1] <- "%"
    }
   }
  }

  for (i in 1:n_x_levels) {

   table1[n_z_header+i+1,1] <- x_levels_labs[i]

   if (yvar!="") {

    for (z in 1:n_z_levels) {

     for (j in 1:n_y_levels) {

      table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2]   <- "0"
      table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2+1] <- "-"
      if (zvar=="") {
       # Absolute n
       table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2]  <-paste(format(sum(data_nona[data_nona[,c(xvar)]==x_levels[i] & data_nona[,c(yvar)]==y_levels[j],"COUNT"]),big.mark=","),sep="")
       # Percentage
       if (sum(data_nona[data_nona[,c(yvar)]==y_levels[j],"COUNT"])>0) {
        table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2+1]<-paste(format(round( sum(data_nona[data_nona[,c(xvar)]==x_levels[i] & data_nona[,c(yvar)]==y_levels[j],"COUNT"]) / sum(data_nona[data_nona[,c(yvar)]==y_levels[j],"COUNT"])*100,1),nsmall=1),sep="")
       }
      } else {
       # Absolute n
       table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2]  <-paste(format(sum(data_nona[data_nona[,c(xvar)]==x_levels[i] & data_nona[,c(yvar)]==y_levels[j] & data_nona[,c(zvar)]==z_levels[z],"COUNT"]),big.mark=","),sep="")
       # Percentage
       if (sum(data_nona[data_nona[,c(yvar)]==y_levels[j] & data_nona[,c(zvar)]==z_levels[z],"COUNT"])>0) {
        table1[n_z_header+i+1,(z-1)*n_y_levels*2+j*2+1]<-paste(format(round( sum(data_nona[data_nona[,c(xvar)]==x_levels[i] & data_nona[,c(yvar)]==y_levels[j] & data_nona[,c(zvar)]==z_levels[z],"COUNT"]) / sum(data_nona[data_nona[,c(yvar)]==y_levels[j] & data_nona[,c(zvar)]==z_levels[z],"COUNT"])*100,1),nsmall=1),sep="")
       }
      }

     }
    }

   }

  }

  # Row Total
  if (zvar!="") {
   for (i in 1:n_z_header) {
    table1[i,n_z_levels*n_y_levels*2+2]   <- ""
    table1[i,n_z_levels*n_y_levels*2+3]   <- ""
   }
  }
  table1[n_z_header+1,n_z_levels*n_y_levels*2+2]   <- "N"
  table1[n_z_header+1,n_z_levels*n_y_levels*2+3]   <- "%"
  for (i in 1:length(x_levels)) {
   table1[n_z_header+i+1,n_z_levels*n_y_levels*2+3]<-"-"
   table1[n_z_header+i+1,n_z_levels*n_y_levels*2+2]<-paste(format(sum(data_nona[data_nona[,c(xvar)]==x_levels[i],"COUNT"]),big.mark=","),sep="")
   if (sum(data_nona[,"COUNT"])>0) {
    table1[n_z_header+i+1,n_z_levels*n_y_levels*2+3]<-paste(format(round( sum(data_nona[data_nona[,c(xvar)]==x_levels[i],"COUNT"])/sum(data_nona[,"COUNT"])*100,1),nsmall=1),sep="")
   }
  }

  # Column Total
  if (n_y_levels>0) {
   for (z in 1:n_z_levels) {
    for (j in 1:n_y_levels) {
     table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2]   <- "0"
     table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2+1] <- "-"
     if (zvar=="") {
       table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2]  <-paste(format(sum(data_nona[data_nona[,c(yvar)]==y_levels[j],"COUNT"]),big.mark=","),sep="")
       if (sum(data_nona[,"COUNT"])>0) {
        table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2+1]<-paste(format(round(sum(data_nona[data_nona[,c(yvar)]==y_levels[j],"COUNT"])/sum(data_nona[,"COUNT"]),2)*100,nsmall=1),sep="")
       }
     } else {
      if (length( data_nona[data_nona[,c(yvar)]==y_levels[j] & data_nona[,c(zvar)]==z_levels[z],] )>0) {
       table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2]  <-paste(format(sum(data_nona[data_nona[,c(yvar)]==y_levels[j]  & data_nona[,c(zvar)]==z_levels[z],"COUNT"]),big.mark=","),sep="")
       if (sum(data_nona[,"COUNT"])>0) {
        table1[n_z_header+n_x_levels+2,(z-1)*n_y_levels*2+j*2+1]<-paste(format(round(sum(data_nona[data_nona[,c(yvar)]==y_levels[j]  & data_nona[,c(zvar)]==z_levels[z],"COUNT"])/sum(data_nona[,"COUNT"]),2)*100,nsmall=1),sep="")
       }
      }
     }
    }
   }
  }

  # Grand Total
  table1[n_z_header+n_x_levels+2,n_z_levels*n_y_levels*2+2]<-paste(format(sum(data_nona[,"COUNT"]),big.mark=","),sep="")

  if (zvar!="") {

   if (n_z_levels*n_y_levels>8) {
    tablerole<-"extrasmall"
    width_1<-"60pt"
    width_2<-"31pt"
    width_3<-"21pt"
   } else {
    tablerole<-"verysmall"
    width_1<-"60pt"
    width_2<-"50pt"
    width_3<-"40pt"
  }
  } else {
   if (yvar!="") {
    tablerole<-"small"
    width_1<-"100pt"
    width_2<-"50pt"
    width_3<-"35pt"
   } else {
    tablerole<-"normal"
    width_1<-"120pt"
    width_2<-"80pt"
    width_3<-"50pt"
   }
  }

  writeTable(
     file=file,
     tablerole=tablerole,
     data=table1,

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

     vars=paste("X",seq(1,n_z_levels*n_y_levels*2+3),sep=""),
     headlabs=paste("X",seq(1,n_z_levels*n_y_levels*2+3),sep=""),
     headwidt=c(width_1,rep(c(width_2,width_3),n_z_levels*n_y_levels+1)),
     colalign=c("left",rep("right",n_z_levels*n_y_levels*2+2)),
     varcolalign="",
     footlabs=NULL,
     footnote=footnote,
     title=title,
     section=section,
     graph=NULL,
     append=append
     )

}

####################################################################
# NeuBIRO_Categorical()
#
# scope
#
# arguments:
####################################################################

NeuBIRO_Categorical<-function(
            file="report.xml",
            append=append,
            data=NULL,
            xvar="",
            xvar_label="",
            x_levels=c(),
            x_levels_labs="",
            yvar="",
            yvar_label="",
            y_levels=c(),
            y_levels_labs="",
            zvar="",
            zvar_label="",
            z_levels=c(),
            z_levels_labs="",
            footnote="Table x.x - Footnote",
            title="x.x.x.x Variable_Name",
            section="Section x.x.x Section_Title"
            ) {

 data[,c(paste(xvar,"_MISS",sep=""))]<-ifelse(data[,c(xvar)]=="","1","0")
 if (yvar!="")  {
  data[,c(paste(yvar,"_MISS",sep=""))]<-ifelse(data[,c(yvar)]=="","1","0")
  yv<-paste(yvar,"_MISS",sep="")
  yl=c("0","1")
  yll<-c("Valid","Missing")
 } else {
  yv<-""
  yl=c()
  yll<-""
 }
 
 # Missing
 NeuFreq(file=file,
         data=data,
         xvar=paste(xvar,"_MISS",sep=""),
         xvar_label=xvar_label,
         x_levels=c("0","1"),
         x_levels_labs=c("Valid","Missing"),
         yvar=yv,
         yvar_label=yvar_label,
         y_levels=yl,
         y_levels_labs=yll,
         zvar=zvar,
         zvar_label=zvar_label,
         z_levels=z_levels,
         z_levels_labs=z_levels_labs,
         footnote=footnote,
         title=paste(title,": Missing values",sep=""),
         section=section,
         append=append)

 # Valid
 NeuFreq(file=file,
         data=data,
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
         title=paste(title,": Frequency Table",sep=""),
         section="",
         append=1)
         
}          

####################################################################
# BIRO_Indicator_Prolog()
#
# scope
#
# arguments:
####################################################################

BIRO_Indicator_Prolog <- function(
 verbose=1,
 id="",
 id_file="",
 infile="",
 writecsv=1,
 title="",
 fileconn="") 
 {

 fileConn<-file("preamble.xml")
 writeLines("",fileConn)
 close(fileConn)
 fileConn<-file("preamble.xml",open="at")
 writeLines(paste('<?dbhtml filename="Indicator_',id_file,'.html" dir="html"?>',sep=""),fileConn)
 close(fileConn)

 if (title!="") {
  writeTitle(title=title,subtitle="",fileConn="report.xml")  
 }

 if (exists("selector")==TRUE) {select_unit<-""}
 
 if (verbose>0) {
  cat("########################################\r")
  if (language=="it") {
   cat(paste("Indicatore ",id,": avvio elaborazione\r",sep=""))
  } else if (language=="en") {
   cat(paste("Indicator ",id,": started processing\r",sep=""))
  }
  cat("########################################\r")
 }

 # Set the working directory
 setwd(workDir) 

 if (engine_type=="local")  {
  input_data<-read.table(infile,header=TRUE,sep = ",",colClasses="character")
 } else if (engine_type=="central") {
  input_data<-BIRO_createCentralData(input_files=input_files,table=paste("statistical_objects/",id,"/BIRO_table_",id_file,sep=""))
 }
 
 # Load data
 input_data <- make_numeric(input_data, c("COUNT"))

 if (select_unit!="") {
  if (verbose>0) {
   if (language=="it") {
    cat(paste("N=",sum(input_data[,"COUNT"])," osservazioni originariamente presenti in dati in Input\r",sep=""))
   } else if (language=="en") {
    cat(paste("N=",sum(input_data[,"COUNT"])," observations originally present in Input data\r",sep=""))
   }
  }
  # Select data
  input_data<-do.call(subset,list(x=input_data,subset=parse(text=select_unit)))
 }

 if (reference!="") {

  arrow_up  <-"<graphic fileref='resources/arrow_up.png'></graphic>"
  arrow_down<-"<graphic fileref='resources/arrow_down.png'></graphic>"

  if (reference=="_internal_") {  ### Reference population is whole input dataset
   ref_data<-input_data
  } else if (reference=="_external_") {   ### Reference population is a collection of output datasets created by Neubiro
   ref_data<-BIRO_createCentralData(input_files=reference_files,table=paste("statistical_objects/",id,"/BIRO_table_",id_file,sep=""))
  }

 }

 if (verbose>0) { 
  cat("\r")
  if (language=="it") {
    cat(paste("N=",sum(input_data[,"COUNT"])," osservazioni caricate da dati in Input\r",sep=""))
  } else if (language=="en") {
   cat(paste("N=",sum(input_data[,"COUNT"])," observations loaded from Input data\r",sep=""))
  }
  if (select_unit!="") {
   if (language=="it") {
    cat(paste("** per effetto della selezione: ",select_unit,"\r",sep=""))
   } else if (language=="en") {
    cat(paste("** due to selection: ",select_unit,"\r",sep=""))
   }
  }
 }

 if (writecsv==1) {
  dir.create(paste(workDir,"/statistical_objects",sep=""))
  write.csv(input_data,paste(workDir,"/statistical_objects/BIRO_table_",id_file,".csv",sep=""),row.names=FALSE,na="")
 }
 
 assign("input_data",input_data,envir=.GlobalEnv)
 assign("select_unit",select_unit,envir=.GlobalEnv)
 if (reference!="") {
  assign("ref_data",ref_data,envir=.GlobalEnv)
 }

}

####################################################################
# BIRO_Indicator_Epilog()
#
# scope
#
# arguments:
####################################################################

BIRO_Indicator_Epilog <- function(
 verbose=0,
 id="",
 id_file="") {

 if (verbose>0) {
  cat("\r")
  if (language=="it") {
   cat(paste("Fine elaborazione Indicatore ",id,sep=""))
  } else if (language=="en") {
   cat(paste("End processing Indicator ",id,sep=""))
  }
  cat("\r")
 }
 
}

####################################################################
# BIRO_CreateCentralData()
#
# scope
#
# arguments:
####################################################################

BIRO_createCentralData<- function(input_files,table,list_numvars=c("COUNT")) {

 for (i in 1:length(input_files)) {
  file_a<-read.table(unzip(input_files[i],paste(table,".csv",sep=""),exdir="./tmp"),header=TRUE,sep=",",colClasses="character")
  file.remove(paste("./tmp/",table,".csv",sep=""))
  if (i>1) {output_data<-rbind(output_data,file_a)} else {output_data<-file_a}
  rm(file_a)
  if (i==length(input_files)) {unlink("./tmp",recursive=TRUE)}
 }

 if (!is.null(list_numvars)==TRUE) {
  output_data<-make_numeric(output_data,list_numvars)
 }

 output_data

}

####################################################################
# BIRO_Minidot()
#
# scope
#
# arguments:
####################################################################

BIRO_Minidot<- function(file,mean,n,se=NULL,min=NULL,max=NULL,i_std=2,sig_column="sig") {

  x<-data.frame(matrix(ncol=3,nrow=length(mean)))
  names(x)<-c("mean","n","se")

  for (i in 1:length(mean)) {
   x[i,"mean"]<-mean[i]
   x[i,"n"]   <-n[i]
  }

  if (is.null(se)) {
    x$n<-ifelse(x$n==0,0.5,x$n)
    x$se<-sqrt((x$mean*(1-x$mean/100))/x$n)
    x$LL <- x$mean-1.96*x$se
    x$UL <- x$mean+1.96*x$se
    min<-0
    max<-100
  } else {
    x$se<-se
    x$LL <- x$mean-1.96*x$se
    x$UL <- x$mean+1.96*x$se
    if (is.null(min)) {
     min<-min(x$LL)-0.5*(max(x$UL)-min(x$LL))
     max<-max(x$UL)+0.5*(max(x$UL)-min(x$LL))
    }
  }

  x$color<-"blue"          # all values
  x$pch<-20                # all values
  x$order<-1

  if (i_std>0) {
    x$color[i_std]<-"red"   # reference standard
    x$pch[i_std]<-18        # reference standard
    x$order[i_std]<-0
    lower_ci<-x$LL
    upper_ci<-x$UL
    i_std<-1
  }

  x<-x[order(x$order),]

  if (i_std>0) {
   x$sig_col<-ifelse(x$LL>upper_ci,1,0)
   x$sig_col<-ifelse(x$UL<lower_ci,2,x$sig_col)
   sig_col<-x[row.names(x)>1,"sig_col"]
   assign(sig_column,sig_col,envir=.GlobalEnv)
  }

  bars<-c(min+0.1,min+((max-min)/5),(min+((max-min)/5)*2),min+((max-min)/5)*3,min+((max-min)/5)*4,max-0.1)
  ######################

  png(file=paste(file,".png",sep=""),width=240,height=60)
  par(mar=c(0,0,0,0))
  dotchart(x$mean,cex=1.5,pch=x$pch,xlim=c(min,max),main="",xlab=,gcolor="black",color=x$color)

  for (i in 1:nrow(x)){lines(x=c(x$LL[i],x$UL[i]), y=c(i,i),lwd=0.5,lend=1)}
  for (i in 1:6) {
   lines(x=c(bars[i],bars[i]),y=c(0,100),lwd=0.1,lend=1)
  }

  if (i_std>0) {
    polygon(c(x$LL[i_std],x$LL[i_std],x$UL[i_std],x$UL[i_std]),c(0,100,100,0),col=rgb(0.12,0.30,1,0.3),border=FALSE)
    polygon(c(0,x$LL[i_std],x$LL[i_std],0),c(0,0,100,100),col=rgb(0.12,0.56,1,0),border=FALSE)
    polygon(c(100,x$UL[i_std],x$UL[i_std],100),c(0,0,100,100),col=rgb(0.12,0.56,1,0),border=FALSE)
  }

  dev.off()

  ######################

  pdf(file=paste(file,".pdf",sep=""),width=0.8,height=0.12)
  par(mar=c(0,0,0,0))
  dotchart(x$mean,cex=0.5,pch=x$pch,xlim=c(min,max),main="",xlab=,gcolor="black",color=x$color)

  for (i in 1:nrow(x)){lines(x=c(x$LL[i],x$UL[i]), y=c(i,i),lwd=0.5,lend=1)}
  for (i in 1:6) {
   lines(x=c(bars[i],bars[i]),y=c(0,100),lwd=0.1,lend=1)
  }

  if (i_std>0) {
    polygon(c(x$LL[i_std],x$LL[i_std],x$UL[i_std],x$UL[i_std]),c(0,100,100,0),col=rgb(0.12,0.30,1,0.3),border=FALSE)
    polygon(c(0,x$LL[i_std],x$LL[i_std],0),c(0,0,100,100),col=rgb(0.12,0.56,1,0),border=FALSE)
    polygon(c(100,x$UL[i_std],x$UL[i_std],100),c(0,0,100,100),col=rgb(0.12,0.56,1,0),border=FALSE)
  }

  dev.off()

  rm(x)

}
