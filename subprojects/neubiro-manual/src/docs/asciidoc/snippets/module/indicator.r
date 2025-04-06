# =====================================================
# Entry point
#
# baseDir = Root directory for all indicators
# workDir = Work directory for this indicator
#
# =====================================================

# tag::content[]
source(paste(baseDir, "/commons/tools.r", sep="")) # <1>

source(paste(baseDir,"/module/implementation.r", sep="")) # <2>

indicator1() # <3>

rm(list=ls(all=TRUE)) # <4>
# end::content[]
