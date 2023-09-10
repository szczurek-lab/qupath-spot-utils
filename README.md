# qupath-spot-utils
Scripts for processing microarray spots in QuPath

### Citation
If you use this package in your work, please cite:

Geras, A., Darvish Shafighi, S., Domżał, K. et al. Celloscope: a probabilistic model for marker-gene-driven cell type deconvolution in spatial transcriptomics data. Genome Biol 24, 120 (2023). [https://doi.org/10.1186/s13059-023-02951-8](https://doi.org/10.1186/s13059-023-02951-8)

### *extract_annotated_spots*

This script finds spots that intersect annotated regions within a slide and saves their positions + annotated labels in a CSV file.

**Input format:**

Spot coordinates file:
```
ACGCCTGACACGCGCT-1,0,0,0,947,1161
TACCGATCCAACACTT-1,0,1,1,1099,1248
...
ATACCCTGGCTCAAAT-1,0,1,13,1101,2295
GGGTTTCCGGCTTCCA-1,0,0,14,950,2383
```

Scale factor file:
```
{"spot_diameter_fullres": 113.41830135687084, "tissue_hires_scalef": 0.1370614, "fiducial_diameter_fullres": 183.2141791149452, "tissue_lowres_scalef": 0.04111842}
```

**Usage:**
1) Load an annotated tissue slide in QuPath
2) Click *Automate -> Show script editor* in QuPath's menu
3) In the Script Editor window, click *File -> Open...* and choose the *extract_annotated_spots.groovy* file
4) Navigate to the bottom of the file and fill in the proper file paths for SPOT_COORDINATES_FILE_PATH, SCALE_FACTOR_FILE_PATH and OUTPUT_FILE_PATH
5) Click *Run -> Run* in the Script Editor window

### *cell_detection*

This script counts the cells for each provided spot coordinate.

**Input format:**
Spot coordinates file:
```
0x0,947,1161
0x1,1099,1248
...
10x10,1101,2295
10x11,950,2383
```

**Usage:**
1) Load a tissue slide in QuPath
2) Click *Automate -> Show script editor* in QuPath's menu
3) In the Script Editor window, click *File -> Open...* and choose the *cell_detection.groovy* file
4) Replace PATH_TO_COORDINATES_FILE with the path to the corresponding spot coordinates file and PATH_TO_OUTPUT_FILE with your desired out output file path
5) Click *Run -> Run* in the Script Editor window
