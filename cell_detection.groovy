import qupath.lib.objects.PathObjects
import qupath.lib.roi.ROIs

setImageType('BRIGHTFIELD_H_E')
setColorDeconvolutionStains(
    """
    {
        "Name" : "H&E default", 
        "Stain 1" : "Hematoxylin", 
        "Values 1" : "0.65111 0.70119 0.29049 ", 
        "Stain 2" : "Eosin", 
        "Values 2" : "0.2159 0.8012 0.5581 ", 
        "Background" : "255 255 255"
    }"""
)

String COORDINATES_FILE = 'PATH_TO_COORDINATES_FILE'
double SPOT_WIDTH = 89.47
double SPOT_HEIGHT = 89.47

@Immutable
class WatershedCellDetectionConfig {
    String detectionImageBrightfield = "Hematoxylin OD"
    double backgroundRadius = 35.0
    double medianRadius = 0.0
    double sigma = 3.0
    double minArea = 50.0
    double maxArea = 1000.0
    double threshold = 0.4
    double maxBackground = 2.0
    boolean watershedPostProcess = true
    double cellExpansion = 5.0
    boolean includeNuclei = true
    boolean smoothBoundaries = true
    boolean makeMeasurements = true
}

@Immutable
class SpotCoordinate {
    String spotId
    double spotX
    double spotY
}

coordinates = new File(COORDINATES_FILE)
    .readLines()
    .collect {
        splitLine = it.split(',')
        new SpotCoordinate(
            spotId: splitLine[0],
            spotX: splitLine[1].toDouble(),
            spotY: splitLine[2].toDouble()
        )
    }

for (def item : coordinates) {
    print spotId
    
    def roi = ROIs.createEllipseROI(
        item.spotY, 
        item.spotX, 
        SPOT_HEIGHT, 
        SPOT_WIDTH, 
        getCurrentViewer().getImagePlane()
    )
    
    def annotation = PathObjects.createAnnotationObject(roi)
    addObjects(annotation)
    selectAnnotations()
    
    runPlugin(
        'qupath.imagej.detect.cells.WatershedCellDetection', 
        JsonOutput.toJson(new WatershedCellDetectionConfig())
    )

    clearAnnotations()
}

clearAnnotations()