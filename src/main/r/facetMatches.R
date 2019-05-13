# 
# This script plots the number of hits and misses using different tipes of matching for a specific dataset
#
start_time <- Sys.time()

#!/usr/bin/env Rscript
args = commandArgs(trailingOnly=TRUE)

# Libraries

library(ggplot2)
library(cowplot)
library(plyr)

# Parameters

## Input files

if (length(args) < 4) {
  stop("The arguments required are: percentages file and plot file name for multiproteoform proteins and phosphoproteoforms. \n", call.=FALSE)
}

# 1 Multiproteoforms original matches file
# 2 Multiproteoforms original matches plot
# 3 Multiproteoforms non-original matches file
# 4 Multiproteoforms non-original matches plot
# 5 Multiproteoforms all matches file
# 6 Multiproteoforms all matches plot
# 7 Phosphoproteoforms matches file
# 8 Phosphoproteoforms plot

## Plot theme

theme_set(theme_bw(base_size = 11))


##################
original.matches.file <- args[1]
original.plot.file <- args[2]

non.original.matches.file <- args[3]
non.original.plot.file <- args[4]

# original.matches.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\multiproteoformProteins_originalMatchesFile.csv"
# original.plot.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteinsOriginal.png"
# 
# non.original.matches.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\resources\\sensitivity\\multiproteoformProteins_nonOriginalMatchesFile.csv"
# non.original.plot.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteinsNonOriginal.png"

matches.original <- read.csv(original.matches.file, header = T)
matches.original$Type <- "original"

matches.others <- read.csv(non.original.matches.file, header = T)
matches.others$Type <- "others"

matches <- rbind(matches.original, matches.others)
matches <- matches[which(matches$Category == "hit"),]

# matches.plot <- dotsAndBoxes(matches)
subdata <- matches[ which(matches$Type == "others"),]
subdata <- ddply(subdata, .(MatchType), summarize,  Percentage=mean(Percentage))
matches$MatchType <- factor(matches$MatchType, levels = subdata$MatchType[order(-subdata$Percentage)])
matches.plot <- ggplot(matches, aes(x=MatchType, y=Percentage, fill=Type)) +
  geom_boxplot(aes(fill = Type), position = position_dodge(0.0), width = 1.2, size = 0.5) +
  geom_dotplot(binaxis='y', stackdir='center', dotsize = 0.5, position = position_dodge(0.0)) +
  theme(axis.text = element_text(angle = 90, hjust = 1)) + ylim(0.0, 100.0)
matches.plot

png(original.plot.file, height = 12, width = 12, units = "cm", res = 600)
plot(matches.plot)
dummy <- dev.off()

# grid_original <- plot_grid(matches.original.plot, matches.non.original.plot, labels = c("A", "B"))
# grid_original
# save_plot("C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\facetOriginals.png", grid_original,
#           ncol = 2, nrow =1,
#           base_aspect_ratio = 1:1
# )

# Facet multiproteoforms and phosphoproteoforms

multi.matches <- args[5]
multi.plot.file <- args[6]

phospho.matches <- args[7]
phospo.plot.file <- args[8]

# multi.matches <- "src\\test\\resources\\sensitivity\\multiproteoformProteins_matchesFile.csv"
# multi.plot.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\multiproteoformProteins.png"

# phospho.matches <- "src\\test\\resources\\sensitivity\\phosphoproteoforms_matchesFile.csv"
# phospo.plot.file <- "C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\phosphoproteoforms.png"

dots <- function (file.matches) {
  matches <- read.csv(file.matches, header = T)
  subdata <- matches[ which(matches$Category == "hit"),]
  subdata <- ddply(subdata, .(MatchType), summarize,  Percentage=mean(Percentage))
  matches$MatchType <- factor(matches$MatchType, levels = subdata$MatchType[order(subdata$Percentage)])
  p <- ggplot(matches, aes(x=MatchType, y=Percentage, fill=Category)) +
    geom_dotplot(binaxis='y', stackdir='center', dotsize = 1, position = position_dodge(0.0)) +
    theme(axis.text = element_text(angle = 90, hjust = 1)) + ylim(0.0, 100.0)
  p
}

multi.plot <- dots(multi.matches)
multi.plot

phospo.plot <- dots(phospho.matches)
phospo.plot

grid_comparison <- plot_grid(multi.plot, phospo.plot, labels = c("A", "B"))
grid_comparison
save_plot("C:\\git\\PathwayAnalysisPlatform\\PathwayMatcher_Publication\\docs\\facetMatches.png", grid_comparison,
          ncol = 2, nrow =1,
          base_aspect_ratio = 1:1
)

print("Finished")
print(Sys.time() - start_time)
