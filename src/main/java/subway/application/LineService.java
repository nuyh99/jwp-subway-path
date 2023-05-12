package subway.application;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import subway.dao.LineDao;
import subway.dao.PathDao;
import subway.dao.StationDao;
import subway.domain.Line;
import subway.domain.Path;
import subway.domain.Paths;
import subway.domain.Station;
import subway.dto.LineRequest;
import subway.dto.LineResponse;
import subway.dto.PathRequest;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class LineService {
    private final LineDao lineDao;
    private final StationDao stationDao;
    private final PathDao pathDao;

    public LineService(LineDao lineDao, final StationDao stationDao, final PathDao pathDao) {
        this.lineDao = lineDao;
        this.stationDao = stationDao;
        this.pathDao = pathDao;
    }

    @Transactional
    public LineResponse saveLine(LineRequest request) {
        Line persistLine = lineDao.insert(new Line(request.getName(), request.getColor(), new Paths()));
        return LineResponse.from(persistLine);
    }

    public List<LineResponse> findLineResponses() {
        List<Line> persistLines = findLines();
        return persistLines.stream()
                .map(line -> {
                    final Paths paths = pathDao.findByLineId(line.getId());
                    return LineResponse.of(line, paths);
                })
                .collect(Collectors.toList());
    }

    public List<Line> findLines() {
        return lineDao.findAll();
    }

    public LineResponse findLineResponseById(Long id) {
        final Line line = lineDao.findById(id);
        final Paths paths = pathDao.findByLineId(id);

        return LineResponse.of(line, paths);
    }

    @Transactional
    public void updateLine(Long id, LineRequest lineUpdateRequest) {
        lineDao.update(new Line(id, lineUpdateRequest.getName(), lineUpdateRequest.getColor()));
    }

    @Transactional
    public void deleteLineById(Long id) {
        lineDao.deleteById(id);
    }

    @Transactional
    public void addPathToLine(final Long lineId, final PathRequest pathRequest) {
        final Station upStation = stationDao.findById(pathRequest.getUpStationId());
        final Station downStation = stationDao.findById(pathRequest.getDownStationID());
        final Path newPath = new Path(upStation, downStation, pathRequest.getDistance());

        final Paths paths = pathDao.findByLineId(lineId);
        pathDao.save(paths.addPath(newPath), lineId);
    }

    @Transactional
    public void deletePathByStationId(final Long stationId) {
        final Station station = stationDao.findById(stationId);

        final List<Long> lineIds = pathDao.findAllLineIdsByStationId(stationId);
        for (final Long lineId : lineIds) {
            Paths paths = pathDao.findByLineId(lineId);
            paths = paths.removePath(station);

            pathDao.save(paths, lineId);
        }
    }
}
