package pl.ppyrczak.mapmatching.service;

import com.graphhopper.GraphHopper;
import com.graphhopper.routing.util.EncodingManager;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Service;
import pl.ppyrczak.mapmatching.dto.PointDto;
import pl.ppyrczak.mapmatching.model.GpsData;
import pl.ppyrczak.mapmatching.repository.GpsDataRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class GpsService {

    private final GpsDataRepository gpsRepository;
    private final String osmFilePath = "ścieżka do pliku .osm.pbf";
    private final String graphHopperDirectory = "ścieżka do katalogu GraphHopper";

    // Konstruktor z wstrzykiwaniem zależności

    public void processGpsData(List<PointDto> gpsPoints) {
        List<Point> matchedPoints = match(gpsPoints);
        List<GpsData> gpsData = convertToGpsData(matchedPoints);
        gpsRepository.saveAll(gpsData);
    }

    private List<Point> match(List<PointDto> gpsPoints) {
        GraphHopper hopper = new GraphHopper().forServer();
        hopper.init(new CmdArgs()
                .put("graph.flag_encoders", "car")
                .put("datareader.file", osmFilePath)
                .put("graph.location", graphHopperDirectory));
        hopper.importOrLoad();

        EncodingManager em = EncodingManager.create("car");
        FlagEncoder encoder = em.getEncoder("car");
        Weighting weighting = new FastestWeighting(encoder);
        HintsMap hintsMap = new HintsMap().setVehicle("car").setWeighting("fastest");
        Router router = hopper.createRouter(RoutingAlgorithmType.DIJKSTRA_BI, hintsMap);

        List<GPXEntry> inputGPXEntries = gpsPoints.stream()
                .map(p -> new GPXEntry(new GHPoint(p.getLatitude(), p.getLongitude()), p.getTimestamp()))
                .collect(Collectors.toList());

        MapMatching mapMatching = new MapMatching(hopper, router, weighting);
        MatchResult matchResult = mapMatching.doWork(inputGPXEntries);

        return matchResult.getEdgeMatches().stream()
                .map(edgeMatch -> edgeMatch.getEdgeState().getBaseNode())
                .map(node -> new GHPoint(hopper.getGraphHopperStorage().getNodeAccess().getLat(node), hopper.getGraphHopperStorage().getNodeAccess().getLon(node)))
                .map(ghPoint -> new PointDto(ghPoint.getLat(), ghPoint.getLon()))
                .collect(Collectors.toList());
    }



    private List<GPXEntry> convertToGpxEntries(List<PointDto> gpsPoints) {
        // tu powinno nastąpić przekształcenie Twoich obiektów PointDto do listy obiektów GPXEntry.
    }

    private List<GpsData> convertToGpsData(List<Point> points) {
        // tu powinno nastąpić przekształcenie listy obiektów Point na listę obiektów GpsData.
    }
}