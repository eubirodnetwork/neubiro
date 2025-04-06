# tag::content[]
indicator1 <- function(xml=1,graphs=1,output=".",append=0,verbose=1) {

  # Set the working directory
  setwd(workDir)

  # Load data <1>
  if (engine_type == "local")  {
    input_data <- merge_table(c("db_master", "db_demographics"))
    input_data <- make_numeric(input_data, c("SUM_STRANIERI", "COUNT"))
  } else if (engine_type == "central") {
    input_data <- createCentralData(input_files=input_files, list_numvars=list_numvars)
  }

  ##########################################
  # Table 1.1
  ##########################################

  table1_1 <- data.frame(matrix(ncol=16,nrow=4)) # <2>

  names(table1_1) <- c("desc","n","perc","ref_n","ref_perc","delta","hist","deltarrow",
                  "align_1","align_2","align_3","align_4","align_5","align_6","align_7","align_8")
  table1_1[,"align_1"] <- "right"
  table1_1[,"align_2"] <- "right"
  table1_1[,"align_3"] <- "right"
  table1_1[,"align_4"] <- "right"
  table1_1[,"align_5"] <- "right"
  table1_1[,"align_6"] <- "right"
  table1_1[,"align_7"] <- "center"
  table1_1[,"align_8"] <- "center"

  writeTable(file="report.xml", # <3>
    data=table1_1,
    append=append,
    vars=c("desc","n","perc","ref_n","ref_perc","delta","hist","deltarrow"),
    headlabs=headlabs,
    headwidt=c("260pt","60pt","60pt","60pt","60pt","60pt","80pt","15pt"),
    colalign=c("left","right","right","right","right","center","center","center"),
    headalign=c("left","center","center","center","center","center","center","center"),
    varcolalign=c("align_1","align_2","align_3","align_4","align_5","align_6","align_7","align_8"),
    footlabs=footlabs,
    footnote=footnote,
    title=title,
    section=section,
    graph=NULL)
}
# end::content[]
