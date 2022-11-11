import qupath.lib.objects.PathObjects
import qupath.lib.roi.ROIs
import groovy.transform.Immutable
import com.google.gson.GsonBuilder

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
String OUTPUT_FILE = 'PATH_TO_OUTPUT_FILE'
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
    
def gson = new GsonBuilder().create()
def configJson = gson.toJson(new WatershedCellDetectionConfig())

def resultsFile = new File(OUTPUT_FILE)
resultsFile.write('spotId,cellCount\n')

print 'Processing started...'

for (def item : coordinates) {
    print item.spotId
    
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
        configJson
    )
    
    resultsFile.append("${item.spotId},${annotation.getChildObjects().size()}\n")

    clearAnnotations()
}

clearAnnotations()

print 'Processing finished.'