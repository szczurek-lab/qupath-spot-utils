import qupath.lib.objects.PathObjects
import qupath.lib.objects.PathObject
import qupath.lib.roi.ROIs
import qupath.lib.regions.ImagePlane

class Spot {
    private int x
    private int y
    
    public Spot(int x, int y) {
        this.x = x
        this.y = y
    }
    
    def String toString() {
        return "(${x},${y})"
    }
    
    def int getX() {
        return this.x
    }
    
    def int getY() {
        return this.y
    }
}

class SpotLabel {
    private int x
    private int y
    private String label
    
    public SpotLabel(int x, int y, String label) {
        this.x = x
        this.y = y
        this.label = label
    }
    
    public def String toString() {
        return "${x},${y},${label}"
    }
}

class InvalidInputException extends RuntimeException {
    public InvalidInputException(String errorMessage) {
        super(errorMessage)
    }
}

class SpotProcessor {
    private String spotCoordinatesFilePath
    private String scaleFactorFilePath
    private String outputFilePath
    private boolean drawSpotAnnotations
    
    private static final String SPOT_ANNOTATION_TYPE = "Ellipse"

    public SpotProcessor(
        String spotCoordinatesFilePath, 
        String scaleFactorFilePath, 
        String outputFilePath, 
        boolean drawSpotAnnotations = false
    ) {
        this.spotCoordinatesFilePath = spotCoordinatesFilePath
        this.scaleFactorFilePath = scaleFactorFilePath
        this.outputFilePath = outputFilePath
        this.drawSpotAnnotations = drawSpotAnnotations
        
        validateInput()
    }
    
    public def processSlide() {
        def spots = createSpotsFromCoordinatesFile(this.spotCoordinatesFilePath)
        def spotDiameter = readSpotDiameter(this.scaleFactorFilePath)
    
        removeExistingSpotAnnotations()  
        processSpotsAndDrawAnnotations(spots, spotDiameter, this.drawSpotAnnotations)
        
        def resultList = prepareListOfSpotsInsideAnnotations(spots, spotDiameter)
        
        def file = new File(this.outputFilePath)
        file.withWriter { writer ->
            writer.writeLine("xCoordinate,yCoordinate,label")
            for (result in resultList) {
                writer.writeLine(result.toString())
            }
        }
    }
    
    private def validateInput() {
        if (!this.spotCoordinatesFilePath?.trim() || !this.scaleFactorFilePath?.trim() || !this.outputFilePath.trim()) {
            throw new InvalidInputException("Invalid input, make sure that all file paths are set!")
        }
    }

    private def List<Spot> createSpotsFromCoordinatesFile(String filePath) {
        def file = new File(filePath)
        def spots = new ArrayList<Spot>()
        
        file.withReader { reader -> 
            def lines = reader.readLines()
            for (line in lines) {
                def splitLine = line.split(",")
                def y = splitLine[4] as int
                def x = splitLine[5] as int
                
                spots.add(new Spot(x, y))
            }
        }
        
        return spots
    }

    private def float readSpotDiameter(String filePath) {
        def file = new File(filePath)
        def spotDiameter = null
        
        file.withReader { reader -> 
            def line = reader.readLine()
            def gson = GsonTools.getInstance(true)
            def type = new HashMap<String, String>().getClass()
            spotDiameter = gson.fromJson(line, type)["spot_diameter_fullres"] as float
        }
        
        return spotDiameter
    }

    private def processSpotsAndDrawAnnotations(List<Spot> spots, float spotDiameter, boolean drawAnnotations) {
        def plane = ImagePlane.getPlane(0, 0)
        def imageHeight = getCurrentImageData().getServer().getHeight()
        def imageWidth = getCurrentImageData().getServer().getWidth()
    
        for (def spot in spots) {
            if (!drawAnnotations) {
                continue
            }
            
            def roi = ROIs.createEllipseROI(
                spot.getX(), 
                spot.getY(), 
                spotDiameter, 
                spotDiameter, 
                plane
            )
            
            def annotation = PathObjects.createAnnotationObject(roi)
            addObject(annotation)
        }
    }

    private def boolean isSpotIntersectingAnnotation(Spot spot, PathObject annotation, float spotDiameter) {
        def plane = ImagePlane.getPlane(0, 0)
        def roi = ROIs.createEllipseROI(
                spot.getX(), 
                spot.getY(), 
                spotDiameter, 
                spotDiameter, 
                plane
            )
       def radius = spotDiameter / 2.0
            
       def circlePoints = new ArrayList<Tuple2<float, float>>()
       def angleStep = 2 * Math.PI / 128.0
       def angle = 0
       
       while (angle < 2 * Math.PI) {
           circlePoints.add(new Tuple2<>(
               spot.getX() + radius + radius * Math.cos(angle),
               spot.getY() + radius + radius * Math.sin(angle)
           ))
           angle += angleStep
       }
            
       for (point in circlePoints) {
           if (annotation.getROI().contains(point[0], point[1])) {
               return true
           }
       }
            
       return false
    }

    private def List<SpotLabel> prepareListOfSpotsInsideAnnotations(List<Spot> spots, float spotDiameter) {
        def resultList = new ArrayList<SpotLabel>()
        
        for (annotation in getAnnotationObjects()) {
            if (annotation.toString().contains(SPOT_ANNOTATION_TYPE)) {
                continue
            }
            
            for (def spot in spots) {
                if (isSpotIntersectingAnnotation(spot, annotation, spotDiameter)) {
                    resultList.add(new SpotLabel(spot.getY(), spot.getX(), annotation.getPathClass().toString()))
                }
            }
        }
        
        return resultList
    }
    
    private def removeExistingSpotAnnotations() {
        for (annotation in getAnnotationObjects()) {
            if (annotation.toString().contains(SPOT_ANNOTATION_TYPE)) {
                removeObject(annotation, false)
            }
        }
    }
}

def DRAW_SPOT_ANNOTATIONS = false
def SPOT_COORDINATES_FILE_PATH = ""
def SCALE_FACTOR_FILE_PATH = ""
def OUTPUT_FILE_PATH = ""

def spotProcessor = new SpotProcessor(
    SPOT_COORDINATES_FILE_PATH,
    SCALE_FACTOR_FILE_PATH,
    OUTPUT_FILE_PATH,
    DRAW_SPOT_ANNOTATIONS
)

spotProcessor.processSlide()

print "Processing finished, output written to ${OUTPUT_FILE_PATH}"