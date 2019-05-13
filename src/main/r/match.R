# 
# This script plots the number of hits and misses using different tipes of matching for a specific dataset
#
start_time <- Sys.time()

#!/usr/bin/env Rscript
args = commandArgs(trailingOnly=TRUE)

# Libraries

library(ggplot2)

# Parameters

## Input files

if (length(args) < 2) {
    stop("The argument proteoform match percentage and the file name for the plot must be provided. \n", call.=FALSE)
}
matchesFile <- args[1]
pngFile <- args[2]

## Plot theme

theme_set(theme_bw(base_size = 11))

# Main script

print(paste("Loading data from", matchesFile))
matches <- read.csv(matchesFile, header = T)
subdata <- matches[matches$Category %in% c("hit"),]
matches$MatchType <- factor(matches$MatchType, levels = subdata$MatchType[order(subdata$Percentage)])

p <- ggplot(matches, aes(x=MatchType, y=Percentage, color=Category)) +
    geom_point() +
    theme(axis.text = element_text(angle = 90, hjust = 1))
p

png(pngFile, height = 12, width = 12, units = "cm", res = 600)
plot(p)
dummy <- dev.off()

print("Finished")
print(Sys.time() - start_time)
